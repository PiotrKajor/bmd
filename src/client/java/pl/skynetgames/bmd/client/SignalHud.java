package pl.skynetgames.bmd.client;

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
import pl.skynetgames.bmd.Emote;
import pl.skynetgames.bmd.Geometry;
import pl.skynetgames.bmd.Sense;
import pl.skynetgames.bmd.net.BmdPayloads;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private static final int ICON = 16;
    private static final int GAP = 3;
    private static final int PAD = 3;

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

        // Siebie rysujemy tylko w widoku z trzeciej osoby - w pierwszej wlasna glowa
        // jest praktycznie w kamerze i babelek nie mialby sie gdzie zmiescic.
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        Vec3 eye = camera.position();

        // Od najdalszego do najblizszego: przy dwoch graczach jeden za drugim
        // babelek blizszego ma byc na wierzchu, a nie pod spodem.
        List<AbstractClientPlayer> visible = new ArrayList<>();
        for (AbstractClientPlayer player : mc.level.players()) {
            if (player.isInvisible()) continue;
            if (player == mc.player && firstPerson) continue;
            visible.add(player);
        }
        visible.sort(Comparator.comparingDouble(
                (AbstractClientPlayer p) -> p.position().distanceToSqr(eye)).reversed());

        for (AbstractClientPlayer player : visible) {
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
            // Babelek zajmuje pas nad punktem y - etykieta klasy schodzi pod niego.
            y += 4;
        }

        if (sense != Sense.NONE) {
            Component tag = Component.literal(sense.icon + " " + sense.pl).withStyle(sense.color);
            int w = mc.font.width(tag) + 6;
            gfx.fill(x - w / 2, y - 1, x + w / 2, y + 10, BUBBLE_BG);
            gfx.centeredText(mc.font, tag, x, y, 0xFFFFFFFF);
        }
    }

    private void drawEmote(GuiGraphicsExtractor gfx, Minecraft mc, int x, int y, Emote emote) {
        drawBubble(gfx, mc, x, y, new ItemStack(emote.icon),
                Component.empty().append(emote.emoji())
                        .append(Component.literal(" " + emote.pl).withStyle(ChatFormatting.WHITE)));
    }

    private void drawItemSign(GuiGraphicsExtractor gfx, Minecraft mc, int x, int y,
                              net.minecraft.resources.Identifier itemId) {
        if (itemId == null || itemId.equals(BmdPayloads.NO_ITEM)) return;
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == Items.AIR) return;
        ItemStack stack = new ItemStack(item);
        drawBubble(gfx, mc, x, y, stack, stack.getHoverName());
    }

    /**
     * Jeden babelek: ikona przedmiotu po lewej, podpis po prawej, tlo dopasowane
     * do obu. Wysokosc bierze sie z ikony (16 px) i wysokosci wiersza tekstu -
     * wczesniej byla wpisana na sztywno i emoji z wlasnego fontu (wyzsze niz
     * zwykla litera) wychodzilo poza tlo.
     */
    private void drawBubble(GuiGraphicsExtractor gfx, Minecraft mc, int x, int y,
                            ItemStack icon, Component label) {
        int textW = mc.font.width(label);
        int inner = ICON + GAP + textW;
        int w = inner + PAD * 2;
        int h = Math.max(ICON, mc.font.lineHeight) + PAD * 2;

        int left = x - w / 2;
        int top = y - h;

        gfx.fill(left, top, left + w, top + h, BUBBLE_BG);

        // Ikona i tekst wysrodkowane w pionie wzgledem siebie, nie wzgledem tla.
        int iconY = top + (h - ICON) / 2;
        int textY = top + (h - mc.font.lineHeight) / 2 + 1;
        gfx.item(icon, left + PAD, iconY);
        gfx.text(mc.font, label, left + PAD + ICON + GAP, textY, 0xFFFFFFFF);
    }

    private static int[] project(Camera camera, int guiW, int guiH, Vec3 target) {
        Entity view = camera.entity();
        Vec3 eye = camera.position();
        return Geometry.project(eye.x, eye.y, eye.z, view.getYRot(), view.getXRot(), camera.getFov(),
                guiW, guiH, target.x, target.y, target.z, MAX_DIST);
    }
}
