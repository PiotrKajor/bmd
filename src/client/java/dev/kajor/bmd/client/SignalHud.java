package dev.kajor.bmd.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import dev.kajor.bmd.Emote;
import dev.kajor.bmd.Geometry;
import dev.kajor.bmd.Sense;
import dev.kajor.bmd.net.BmdPayloads;

import java.util.Map;
import java.util.UUID;

/**
 * Babelki nad glowami: ikona klasy, gest z kola i tabliczka z przedmiotem.
 *
 * Rysowane w HUD z wlasna projekcja swiat->ekran zamiast przez render encji.
 * Dzieki temu mozna uzyc gotowego gfx.item() (prawdziwa ikona przedmiotu)
 * i nie trzeba wchodzic w pipeline renderowania modeli.
 */
public class SignalHud implements HudElement {

    private static final int BUBBLE_BG = 0xB0000000;
    private static final double MAX_DIST = 48.0D;

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        // slepy nic nie widzi, a wlasnej glowy i tak nie oglada
        if (ClientState.mine == Sense.BLIND) return;
        if (ClientState.ROSTER.isEmpty() && ClientState.SIGNALS.isEmpty()) return;

        Camera camera = mc.gameRenderer.mainCamera();
        if (!camera.isInitialized() || camera.entity() == null) return;

        long now = System.currentTimeMillis();
        ClientState.SIGNALS.entrySet().removeIf(e -> e.getValue().expiresAt() < now);

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player == mc.player || player.isInvisible()) continue;
            drawFor(gfx, mc, camera, player, now);
        }
    }

    private void drawFor(GuiGraphicsExtractor gfx, Minecraft mc, Camera camera,
                         AbstractClientPlayer player, long now) {
        UUID id = player.getUUID();
        Sense sense = ClientState.senseOf(id);
        ClientState.Signal signal = ClientState.SIGNALS.get(id);
        if (sense == Sense.NONE && signal == null) return;

        Vec3 head = new Vec3(player.getX(), player.getY() + player.getBbHeight() + 0.85D, player.getZ());
        int[] screen = project(camera, gfx.guiWidth(), gfx.guiHeight(), head);
        if (screen == null) return;

        int x = screen[0];
        int y = screen[1];

        if (signal != null) {
            if (signal.emoteId() >= 0) {
                drawEmote(gfx, mc, x, y, Emote.byId(signal.emoteId()));
            } else {
                drawItemSign(gfx, mc, x, y, signal.itemId());
            }
            y += 22;
        }

        if (sense != Sense.NONE) {
            Component tag = Component.literal(sense.icon + " " + sense.pl).withStyle(sense.color);
            int w = mc.font.width(tag) + 6;
            gfx.fill(x - w / 2, y - 1, x + w / 2, y + 10, BUBBLE_BG);
            gfx.centeredText(mc.font, tag, x, y, 0xFFFFFFFF);
        }
    }

    private void drawEmote(GuiGraphicsExtractor gfx, Minecraft mc, int x, int y, Emote emote) {
        Component label = Component.literal(emote.symbol + " " + emote.pl).withStyle(ChatFormatting.WHITE);
        int w = Math.max(mc.font.width(label) + 24, 40);
        gfx.fill(x - w / 2, y - 22, x + w / 2, y - 2, BUBBLE_BG);
        gfx.item(new ItemStack(emote.icon), x - w / 2 + 2, y - 20);
        gfx.text(mc.font, label, x - w / 2 + 21, y - 15, 0xFFFFFFFF);
    }

    private void drawItemSign(GuiGraphicsExtractor gfx, Minecraft mc, int x, int y, net.minecraft.resources.Identifier itemId) {
        if (itemId == null || itemId.equals(BmdPayloads.NO_ITEM)) return;
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == Items.AIR) return;

        ItemStack stack = new ItemStack(item);
        Component label = stack.getHoverName();
        int w = Math.max(mc.font.width(label) + 24, 40);
        gfx.fill(x - w / 2, y - 22, x + w / 2, y - 2, BUBBLE_BG);
        gfx.item(stack, x - w / 2 + 2, y - 20);
        gfx.text(mc.font, label, x - w / 2 + 21, y - 15, 0xFFFFFFFF);
    }

    private static int[] project(Camera camera, int guiW, int guiH, Vec3 target) {
        Entity view = camera.entity();
        Vec3 eye = camera.position();
        return Geometry.project(eye.x, eye.y, eye.z, view.getYRot(), view.getXRot(), camera.getFov(),
                guiW, guiH, target.x, target.y, target.z, MAX_DIST);
    }
}
