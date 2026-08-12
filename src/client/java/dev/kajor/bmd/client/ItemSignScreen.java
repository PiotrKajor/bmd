package dev.kajor.bmd.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Wyszukiwarka przedmiotow dla niemego: wpisz czego szukasz, kliknij, przedmiot
 * zawisa nad twoja glowa. Czasem "chleb" trzeba pokazac doslownie.
 */
public class ItemSignScreen extends Screen {

    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int CELL = 20;

    private EditBox search;
    private final List<ItemStack> results = new ArrayList<>();

    public ItemSignScreen() {
        super(Component.translatable("bmd.sign.title"));
    }

    @Override
    protected void init() {
        int boxW = COLUMNS * CELL;
        int left = (width - boxW) / 2;
        search = new EditBox(font, left, height / 2 - 60, boxW, 18, Component.translatable("bmd.sign.search"));
        search.setMaxLength(48);
        search.setResponder(this::refresh);
        addRenderableWidget(search);
        setInitialFocus(search);
        refresh("");
    }

    private void refresh(String query) {
        results.clear();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ItemStack stack = new ItemStack(item);
            if (q.isEmpty() || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)
                    || BuiltInRegistries.ITEM.getKey(item).getPath().contains(q)) {
                results.add(stack);
                if (results.size() >= COLUMNS * ROWS) break;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fill(0, 0, gfx.guiWidth(), gfx.guiHeight(), 0xCC000000);
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);

        gfx.centeredText(font, Component.translatable("bmd.sign.search").withStyle(ChatFormatting.YELLOW),
                gfx.guiWidth() / 2, gfx.guiHeight() / 2 - 78, 0xFFFFFFFF);

        int left = (gfx.guiWidth() - COLUMNS * CELL) / 2;
        int top = gfx.guiHeight() / 2 - 34;

        for (int i = 0; i < results.size(); i++) {
            int x = left + (i % COLUMNS) * CELL;
            int y = top + (i / COLUMNS) * CELL;
            boolean hover = mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL;
            gfx.fill(x, y, x + CELL - 2, y + CELL - 2, hover ? 0xAA3A6EA5 : 0x66202020);
            gfx.item(results.get(i), x + 2, y + 2);
            if (hover) {
                gfx.setTooltipForNextFrame(font, results.get(i).getHoverName(), mouseX, mouseY);
            }
        }

        if (results.isEmpty()) {
            gfx.centeredText(font, Component.translatable("bmd.sign.nothing").withStyle(ChatFormatting.DARK_GRAY),
                    gfx.guiWidth() / 2, top + 10, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = (width - COLUMNS * CELL) / 2;
        int top = height / 2 - 34;
        double mx = event.x();
        double my = event.y();

        for (int i = 0; i < results.size(); i++) {
            int x = left + (i % COLUMNS) * CELL;
            int y = top + (i / COLUMNS) * CELL;
            if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                Identifier id = BuiltInRegistries.ITEM.getKey(results.get(i).getItem());
                BmdClient.sendItemSign(id);
                Minecraft.getInstance().gui.setScreen(null);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
