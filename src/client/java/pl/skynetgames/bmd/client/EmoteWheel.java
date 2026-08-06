package pl.skynetgames.bmd.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import pl.skynetgames.bmd.Emote;
import pl.skynetgames.bmd.Geometry;

/**
 * Kolo gestow. Trzymasz klawisz, celujesz myszka, puszczasz - gest leci do serwera.
 * Kliknieciem tez dziala, gdyby ktos wolal.
 */
public class EmoteWheel extends Screen {

    private static final double DEAD_ZONE = 20.0D;

    private final KeyMapping heldKey;
    private int hovered = -1;
    /** Czy klawisz byl trzymany po otwarciu kola - patrz nizej. */
    private boolean keyHeldSinceOpen = false;

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
        // Dwa sposoby uzycia, bo krotkie tapniecie klawisza i trzymanie to co innego:
        //  - trzymasz G, celujesz, puszczasz -> wybor zatwierdzony puszczeniem
        //  - tapnales G -> kolo zostaje otwarte, wybierasz kliknieciem (ESC anuluje)
        // Bez flagi kolo zamykalo sie w pierwszej klatce po tapnieciu (isDown juz false)
        // i wygladalo, jakby klawisz w ogole nie dzialal.
        if (heldKey != null) {
            if (heldKey.isDown()) {
                keyHeldSinceOpen = true;
            } else if (keyHeldSinceOpen) {
                confirm();
                return;
            }
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

            // Samo emoji - ikona przedmiotu byla druga nazwa tego samego i tylko
            // zabierala miejsce, odkad gesty maja wlasne obrazki.
            int half = active ? 20 : 16;
            gfx.fill(x - half, y - half, x + half, y + half, active ? 0xCC3A6EA5 : 0x99202020);
            gfx.centeredText(font, emotes[i].emoji(), x, y - 4, 0xFFFFFFFF);
        }

        Component center = hovered >= 0
                ? Component.empty().append(emotes[hovered].emoji())
                        .append(Component.literal(" " + emotes[hovered].pl).withStyle(ChatFormatting.YELLOW))
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
