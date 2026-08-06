package dev.kajor.bmd.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import dev.kajor.bmd.Emote;
import dev.kajor.bmd.Geometry;

/**
 * Kolo gestow. Trzymasz klawisz, celujesz myszka, puszczasz - gest leci do serwera.
 * Kliknieciem tez dziala, gdyby ktos wolal.
 */
public class EmoteWheel extends Screen {

    private static final double DEAD_ZONE = 20.0D;

    private final KeyMapping heldKey;
    private int hovered = -1;

    public EmoteWheel(KeyMapping heldKey) {
        super(Component.literal("Kolo gestow"));
        this.heldKey = heldKey;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        // Klawisz puszczony = wybor zatwierdzony. Trzymanie i puszczanie jest szybsze
        // niz klikanie, a przy tej mechanice liczy sie kazda sekunda.
        if (heldKey != null && !heldKey.isDown()) {
            confirm();
            return;
        }

        int cx = gfx.guiWidth() / 2;
        int cy = gfx.guiHeight() / 2;
        gfx.fill(0, 0, gfx.guiWidth(), gfx.guiHeight(), 0x88000000);

        Emote[] emotes = Emote.values();
        double radius = Math.min(gfx.guiWidth(), gfx.guiHeight()) * 0.28D;

        hovered = Geometry.sector(mouseX - cx, mouseY - cy, emotes.length, DEAD_ZONE);

        for (int i = 0; i < emotes.length; i++) {
            double angle = Geometry.sectorAngle(i, emotes.length);
            int x = (int) (cx + Math.sin(angle) * radius);
            int y = (int) (cy - Math.cos(angle) * radius);
            boolean active = i == hovered;

            int half = active ? 26 : 20;
            gfx.fill(x - half, y - half, x + half, y + half, active ? 0xCC3A6EA5 : 0x99202020);
            gfx.item(new ItemStack(emotes[i].icon), x - 8, y - 12);
            gfx.centeredText(font, Component.literal(emotes[i].symbol), x, y + 6, 0xFFFFFFFF);
        }

        Component center = hovered >= 0
                ? Component.literal(emotes[hovered].pl).withStyle(ChatFormatting.YELLOW)
                : Component.literal("wybierz kierunek").withStyle(ChatFormatting.DARK_GRAY);
        gfx.centeredText(font, center, cx, cy - 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        confirm();
        return true;
    }

    private void confirm() {
        if (hovered >= 0) {
            BmdClient.sendEmote(hovered);
        }
        Minecraft.getInstance().gui.setScreen(null);
    }
}
