package pl.skynetgames.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import pl.skynetgames.bmd.BlindMode;
import pl.skynetgames.bmd.Geometry;
import pl.skynetgames.bmd.Sense;

/**
 * Czern zaslaniajaca swiat. W odroznieniu od efektu Blindness nie da sie tego
 * obejsc gamma ani shaderem - to zwykly prostokat rysowany w HUD.
 *
 * Domyslnie idzie POD reszta HUD, wiec hotbar, zycie i glod zostaja widoczne:
 * slepy ma nie widziec swiata, a nie wlasnego ekwipunku.
 */
public class BlindHud implements HudElement {

    private static final int BLACK = 0xFF000000;
    private static final long ECHO_LIFETIME_MS = 1400L;

    /** true = warstwa na wierzchu HUD (czern zakrywa tez hotbar), false = pod HUD. */
    private final boolean over;

    public BlindHud(boolean over) {
        this.over = over;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        if (ClientState.mine != Sense.BLIND) return;
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
        gfx.centeredText(mc.font, Component.literal("[T] czat  •  /bmd info"),
                w / 2, h - 12, 0x33FFFFFF);

        if (ClientState.blindMode == BlindMode.HARD || mc.player == null) {
            return;
        }
        drawEchoes(gfx, mc, w, h);
    }

    /**
     * Kazde zrodlo dzwieku to znacznik na okregu wokol srodka ekranu, ustawiony pod
     * katem miedzy kierunkiem patrzenia a kierunkiem do dzwieku.
     *
     * Ksztalt niesie druga informacje - pion: ▲ nad toba, ● na twoim poziomie,
     * ▼ pod toba. Swieze echo jest jasne i ma wokol siebie poswiate, ktora gasnie;
     * dzieki temu widac, ktory dzwiek byl przed chwila, a ktory juz cichnie.
     */
    /**
     * Znacznik ladu je tam, gdzie naprawde jest zrodlo dzwieku - ta sama projekcja,
     * ktorej uzywaja nametagi. Dzieki temu slepy slyszy krok i widzi punkt dokladnie
     * w tym miejscu przestrzeni, a nie na obreczy wokol celownika.
     *
     * Dzwiek za plecami nie ma gdzie wyladowac, wiec laduje na krawedzi ekranu
     * w swoim kierunku - inaczej zniknalby zupelnie.
     *
     * Ksztalt niesie pion: ▲ nad toba, ● na twoim poziomie, ▼ pod toba.
     */
    private void drawEchoes(GuiGraphicsExtractor gfx, Minecraft mc, int w, int h) {
        Camera camera = mc.gameRenderer.mainCamera();
        if (!camera.isInitialized() || camera.entity() == null) return;

        long now = System.currentTimeMillis();
        Vec3 eye = camera.position();
        Entity view = camera.entity();
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        synchronized (ClientState.ECHOES) {
            for (ClientState.Echo echo : ClientState.ECHOES) {
                long left = echo.expiresAt() - now;
                if (left <= 0) continue;

                double dx = echo.x() - px;
                double dy = echo.y() - py;
                double dz = echo.z() - pz;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > ClientState.echoRange) continue;

                float fade = (float) left / ECHO_LIFETIME_MS;
                float near = (float) (1.0D - Math.min(1.0D, dist / ClientState.echoRange));
                int alpha = (int) (255 * Math.min(1.0F, fade * (0.45F + 0.55F * near)));
                if (alpha < 16) continue;

                int[] p = Geometry.project(eye.x, eye.y, eye.z, view.getYRot(), view.getXRot(),
                        camera.getFov(), w, h, echo.x(), echo.y(), echo.z(), ClientState.echoRange + 1);

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
    }
}
