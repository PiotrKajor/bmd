package pl.skynetgames.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import pl.skynetgames.bmd.Sense;
import pl.skynetgames.bmd.goal.GoalTracker;

/**
 * Licznik czasu i cel w prawym dolnym rogu.
 *
 * Czas liczy klient od znacznika startu, wiec chodzi plynnie co klatke i nie
 * wymaga pakietu co sekunde. Po ukonczeniu serwer podaje czas koncowy i licznik
 * zastyga na wyniku.
 *
 * Slepy tego nie zobaczy - jego ekran zaslania czern, a licznik to informacja
 * wzrokowa jak kazda inna.
 */
public class GoalHud implements HudElement {

    private static final int MARGIN = 4;

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        if (ClientState.goalId == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (ClientState.mine == Sense.BLIND) return;

        boolean done = ClientState.goalFinishedMs > 0L;
        long ms = done ? ClientState.goalFinishedMs
                : Math.max(0L, System.currentTimeMillis() - ClientState.goalStartedAt);

        Component title = Component.translatable("bmd.goal." + ClientState.goalId)
                .copy().withStyle(done ? ChatFormatting.GREEN : ChatFormatting.WHITE);
        Component timer = Component.literal(GoalTracker.formatTime(ms))
                .withStyle(done ? ChatFormatting.GREEN : ChatFormatting.GOLD, ChatFormatting.BOLD);

        int w = gfx.guiWidth();
        int h = gfx.guiHeight();
        int line = mc.font.lineHeight;

        // Prawy dolny rog, nad paskiem doswiadczenia i hotbarem
        int bottom = h - MARGIN - 40;
        int timerW = mc.font.width(timer);
        int titleW = mc.font.width(title);
        int boxW = Math.max(timerW, titleW) + MARGIN * 2;
        int boxH = line * 2 + MARGIN * 2;

        int left = w - boxW - MARGIN;
        int top = bottom - boxH;

        gfx.fill(left, top, left + boxW, top + boxH, 0x88000000);
        gfx.text(mc.font, title, left + MARGIN, top + MARGIN, 0xFFFFFFFF);
        gfx.text(mc.font, timer, left + boxW - MARGIN - timerW, top + MARGIN + line, 0xFFFFFFFF);
    }
}
