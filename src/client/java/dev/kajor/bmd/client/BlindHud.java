package dev.kajor.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import dev.kajor.bmd.BlindMode;
import dev.kajor.bmd.Geometry;
import dev.kajor.bmd.Sense;

/**
 * Czern zaslaniajaca swiat. W odroznieniu od efektu Blindness nie da sie tego
 * obejsc gamma ani shaderem - to zwykly prostokat rysowany w HUD.
 *
 * Domyslnie idzie POD reszta HUD, wiec hotbar, zycie i glod zostaja widoczne:
 * slepy ma nie widziec swiata, a nie wlasnego ekwipunku.
 */
public class BlindHud implements HudElement {

    private static final int BLACK = 0xFF000000;
    private static final long ECHO_LIFETIME_MS = Echolocation.CORE_FADE_MS;

    /** Kolor punktu wg powierzchni: 0 podloga (cieplo), 1 sciana (chlodno), 2 sufit. */
    private static final int[] SURFACE = {0xFFD9A0, 0xBFE6FF, 0x9B8BD8};

    /** true = warstwa na wierzchu HUD (czern zakrywa tez hotbar), false = pod HUD. */
    private final boolean over;

    public BlindHud(boolean over) {
        this.over = over;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        if (ClientState.mine != Sense.BLIND || !ClientState.effectsActive()) return;
        // Rysuje tylko warstwa pasujaca do ustawienia serwera, druga milczy.
        if (over == ClientState.showHud) return;

        Minecraft mc = Minecraft.getInstance();
        int w = gfx.guiWidth();
        int h = gfx.guiHeight();

        if (ClientState.blindMode == BlindMode.EASY) {
            // Same Blindness i Darkness zostawiaja sporo widocznosci - dokladamy
            // przyciemnienie, zeby "latwy" nadal znaczyl slepote, a nie mgle.
            int alpha = (int) (255 * Math.clamp(ClientState.easyDarkness, 0.0D, 1.0D));
            if (alpha > 0) gfx.fill(0, 0, w, h, alpha << 24);
            return;
        }

        gfx.fill(0, 0, w, h, BLACK);

        // Jedyne wyjscie z ciemnosci: gracz musi wiedziec, ze da sie otworzyc czat.
        gfx.centeredText(mc.font, Component.translatable("bmd.blind.hint"),
                w / 2, h - 12, 0x33FFFFFF);

        if (ClientState.blindMode == BlindMode.HARD || mc.player == null) {
            return;
        }
        drawEchoes(gfx, mc, w, h);
    }

    /**
     * Kazde echo maluje sie dwoma warstwami.
     *
     * Chmura: punkty, w ktore trafily promienie wystrzelone ze zrodla dzwieku,
     * rzutowane ta sama projekcja, co nametagi - stoja dokladnie tam, gdzie stoi
     * sciana, wiec pokoj czyta sie w trzech wymiarach, a nie jako plamki na obreczy.
     * Fala rozchodzi sie w czasie, wiec ksztalt rozlewa sie od zrodla i gasnie.
     * Kolor niesie orientacje powierzchni: podloga cieplo, sciana chlodno, sufit fiolet.
     * Rozmiar punktu maleje z odlegloscia od oka - to caly efekt glebi.
     *
     * Rdzen: samo zrodlo dzwieku, ▲ nad toba / ● na twoim poziomie / ▼ pod toba.
     * Rzeczy, ktore halasuja, ale nie sa blokami (gracz, mob), nie zostawiaja punktow -
     * ten znacznik jest jedynym, co po nich zostaje. Dzwiek zza plecow nie ma gdzie
     * wyladowac, wiec laduje na krawedzi ekranu w swoim kierunku.
     */
    private void drawEchoes(GuiGraphicsExtractor gfx, Minecraft mc, int w, int h) {
        Camera camera = mc.gameRenderer.mainCamera();
        if (!camera.isInitialized() || camera.entity() == null) return;

        long now = System.currentTimeMillis();
        Vec3 eye = camera.position();
        Entity view = camera.entity();
        double yaw = view.getYRot();
        double pitch = view.getXRot();
        double fov = camera.getFov();
        double range = ClientState.echoRange;

        synchronized (ClientState.ECHOES) {
            for (ClientState.Echo echo : ClientState.ECHOES) {
                if (echo.expiresAt() <= now) continue;
                long age = now - echo.bornAt();
                drawCloud(gfx, echo, age, eye, yaw, pitch, fov, w, h, range);
                drawCore(gfx, mc, echo, age, eye, yaw, pitch, fov, w, h, range);
            }
        }
    }

    private void drawCloud(GuiGraphicsExtractor gfx, ClientState.Echo echo, long age,
                           Vec3 eye, double yaw, double pitch, double fov, int w, int h, double range) {
        float[] cloud = echo.cloud();
        for (int i = 0; i + 4 < cloud.length; i += 5) {
            float fade = Geometry.waveFade(age, cloud[i + 3], Echolocation.WAVE_SPEED, Echolocation.POINT_FADE_MS);
            if (fade <= 0.0F) continue;

            double x = echo.x() + cloud[i];
            double y = echo.y() + cloud[i + 1];
            double z = echo.z() + cloud[i + 2];

            double ex = x - eye.x, ey = y - eye.y, ez = z - eye.z;
            double toEye = Math.sqrt(ex * ex + ey * ey + ez * ez);
            // Bez testu glebi punkt za sciana i tak sie zapali - to celowe: echolokacja
            // ma czuc pokoj obok, nie odwzorowywac wzrok.
            int alpha = (int) (255 * fade * (0.45F + 0.55F * (float) (1.0D - Math.min(1.0D, toEye / range))));
            if (alpha < 12) continue;

            int[] p = Geometry.project(eye.x, eye.y, eye.z, yaw, pitch, fov, w, h, x, y, z, range * 2);
            if (p == null || p[0] < 0 || p[0] >= w || p[1] < 0 || p[1] >= h) continue;

            int size = toEye < 6.0D ? 3 : (toEye < 14.0D ? 2 : 1);
            gfx.fill(p[0], p[1], p[0] + size, p[1] + size, (alpha << 24) | SURFACE[(int) cloud[i + 4]]);
        }
    }

    private void drawCore(GuiGraphicsExtractor gfx, Minecraft mc, ClientState.Echo echo, long age,
                          Vec3 eye, double yaw, double pitch, double fov, int w, int h, double range) {
        if (age >= ECHO_LIFETIME_MS) return;

        double dx = echo.x() - mc.player.getX();
        double dy = echo.y() - mc.player.getY();
        double dz = echo.z() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > range) return;

        float fade = 1.0F - (float) age / ECHO_LIFETIME_MS;
        float near = (float) (1.0D - Math.min(1.0D, dist / range));
        int alpha = (int) (255 * Math.min(1.0F, fade * (0.45F + 0.55F * near)));
        if (alpha < 16) return;

        int[] p = Geometry.project(eye.x, eye.y, eye.z, yaw, pitch, fov, w, h,
                echo.x(), echo.y(), echo.z(), range + 1);

        int x, y;
        if (p != null) {
            x = Math.clamp(p[0], 8, w - 8);
            y = Math.clamp(p[1], 8, h - 8);
        } else {
            // poza kadrem: kierunek w poziomie, znacznik na obrzezu ekranu
            double angle = Math.toRadians(Math.toDegrees(Math.atan2(-dx, dz)) - mc.player.getYRot());
            double r = Math.min(w, h) * 0.44D;
            x = (int) Math.clamp(w / 2 + Math.sin(angle) * r, 8, w - 8);
            y = (int) Math.clamp(h / 2 - Math.cos(angle) * r, 8, h - 8);
        }

        String glyph = dy > 1.5D ? "▲" : (dy < -1.5D ? "▼" : "●");
        int rgb = dy > 1.5D ? 0x9CD2FF : (dy < -1.5D ? 0xFFC48C : 0xFFFFFF);

        int halo = (int) (alpha * 0.35F);
        if (halo > 12) {
            gfx.centeredText(mc.font, Component.literal(glyph), x, y - 1, (halo << 24) | rgb);
            gfx.centeredText(mc.font, Component.literal(glyph), x, y + 1, (halo << 24) | rgb);
        }
        gfx.centeredText(mc.font, Component.literal(glyph), x, y, (alpha << 24) | rgb);
    }
}
