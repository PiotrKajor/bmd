package pl.skynetgames.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import pl.skynetgames.bmd.BlindMode;
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
        // W trybie latwym slepote robia wanilkowe efekty - nie zaslaniamy ekranu.
        if (ClientState.blindMode == BlindMode.EASY) return;
        // Rysuje tylko warstwa pasujaca do ustawienia serwera, druga milczy.
        if (over == ClientState.showHud) return;

        Minecraft mc = Minecraft.getInstance();
        int w = gfx.guiWidth();
        int h = gfx.guiHeight();

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
    private void drawEchoes(GuiGraphicsExtractor gfx, Minecraft mc, int w, int h) {
        long now = System.currentTimeMillis();
        double radius = Math.min(w, h) * 0.38D;
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        float yaw = mc.player.getYRot();

        synchronized (ClientState.ECHOES) {
            for (ClientState.Echo echo : ClientState.ECHOES) {
                long left = echo.expiresAt() - now;
                if (left <= 0) continue;

                double dx = echo.x() - px;
                double dy = echo.y() - py;
                double dz = echo.z() - pz;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > ClientState.echoRange) continue;

                double angleToSound = Math.toDegrees(Math.atan2(-dx, dz));
                double rel = Math.toRadians(angleToSound - yaw);

                float fade = (float) left / ECHO_LIFETIME_MS;
                float near = (float) (1.0D - Math.min(1.0D, dist / ClientState.echoRange));

                // Blizsze dzwieki siadaja blizej srodka - promien niesie odleglosc,
                // wiec nie trzeba jej zgadywac z samej jasnosci.
                double r = radius * (0.45D + 0.55D * (1.0D - near));
                int x = (int) (w / 2 + Math.sin(rel) * r);
                int y = (int) (h / 2 - Math.cos(rel) * r);

                int alpha = (int) (255 * Math.min(1.0F, fade * (0.45F + 0.55F * near)));
                if (alpha < 16) continue;

                String glyph = dy > 1.5D ? "▲" : (dy < -1.5D ? "▼" : "●");
                int rgb = dy > 1.5D ? 0x9CD2FF : (dy < -1.5D ? 0xFFC48C : 0xFFFFFF);

                // Poswiata: ta sama ikona pod spodem, ciemniejsza i lekko przesunieta,
                // daje wrazenie rozchodzacej sie fali bez rysowania osobnego okregu.
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
