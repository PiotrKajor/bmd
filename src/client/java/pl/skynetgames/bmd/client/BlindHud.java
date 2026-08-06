package pl.skynetgames.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import pl.skynetgames.bmd.Sense;

/**
 * Czern rysowana na samym koncu HUD-u. W odroznieniu od efektu Blindness nie da sie
 * tego obejsc gamma, shaderem ani F3 - to zwykly prostokat na wierzchu wszystkiego.
 */
public class BlindHud implements HudElement {

    private static final int BLACK = 0xFF000000;

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        if (ClientState.mine != Sense.BLIND) return;

        Minecraft mc = Minecraft.getInstance();
        int w = gfx.guiWidth();
        int h = gfx.guiHeight();

        gfx.fill(0, 0, w, h, BLACK);

        if (ClientState.hardMode || mc.player == null) {
            gfx.centeredText(mc.font,
                    Component.literal("ciemno"), w / 2, h - 14, 0x22FFFFFF);
            return;
        }

        drawEchoes(gfx, mc, w, h);
    }

    /**
     * Kazde zrodlo dzwieku to znacznik na okregu wokol srodka ekranu, ustawiony pod
     * katem miedzy kierunkiem patrzenia a kierunkiem do dzwieku. Blizej i swiezej = jasniej.
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
                double dz = echo.z() - pz;
                double dy = echo.y() - py;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > ClientState.echoRange) continue;

                // kat wzgledem kierunku patrzenia; 0 = prosto przed siebie
                double angleToSound = Math.toDegrees(Math.atan2(-dx, dz));
                double rel = Math.toRadians(angleToSound - yaw);

                int x = (int) (w / 2 + Math.sin(rel) * radius);
                int y = (int) (h / 2 - Math.cos(rel) * radius);

                float fade = (float) left / 1400.0F;
                float near = (float) (1.0D - Math.min(1.0D, dist / ClientState.echoRange));
                int alpha = (int) (255 * Math.min(1.0F, fade * (0.35F + 0.65F * near)));
                if (alpha < 12) continue;

                // wyzej/nizej ode mnie - inny odcien, zeby dalo sie szukac w pionie
                int rgb = dy > 1.5D ? 0x88CCFF : (dy < -1.5D ? 0xFFAA66 : 0xFFFFFF);
                int color = (alpha << 24) | rgb;

                int size = 2 + (int) (3 * near);
                gfx.fill(x - size, y - size, x + size, y + size, color);
            }
        }
    }
}
