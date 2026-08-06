package dev.kajor.bmd.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import dev.kajor.bmd.BmdMod;
import dev.kajor.bmd.Emote;
import dev.kajor.bmd.Sense;
import dev.kajor.bmd.net.BmdPayloads;

public class BmdClient implements ClientModInitializer {

    private static final long SIGNAL_LIFETIME_MS = 10_000L;

    private static KeyMapping wheelKey;
    private static KeyMapping itemSignKey;

    @Override
    public void onInitializeClient() {
        wheelKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bmd.wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyMapping.Category.GAMEPLAY));
        itemSignKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bmd.item_sign", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyMapping.Category.GAMEPLAY));

        ClientPlayNetworking.registerGlobalReceiver(BmdPayloads.Roster.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    ClientState.ROSTER.clear();
                    ClientState.ROSTER.putAll(payload.senses());
                    ClientState.mine = payload.mine();
                    ClientState.hardMode = payload.hardMode();
                    ClientState.echoRange = payload.echoRange();
                    ClientState.showHud = payload.showHud();
                }));

        ClientPlayNetworking.registerGlobalReceiver(BmdPayloads.Signal.TYPE, (payload, context) ->
                context.client().execute(() -> ClientState.SIGNALS.put(payload.sender(),
                        new ClientState.Signal(payload.emoteId(), payload.itemId(),
                                System.currentTimeMillis() + SIGNAL_LIFETIME_MS))));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientState.reset());

        // Czern musi byc na samym wierzchu, inaczej slepy czyta swoj ekwipunek.
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "signals"), new SignalHud());
        // Dwie warstwy czerni, bo rejestracja HUD jest jednorazowa, a o tym, ktora ma
        // dzialac, decyduje flaga z serwera. "under" rysuje sie przed reszta HUD
        // (hotbar zostaje widoczny), "over" na samym koncu (czern zakrywa wszystko).
        HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "blind_under"), new BlindHud(false));
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "blind_over"), new BlindHud(true));

        ClientTickEvents.END_CLIENT_TICK.register(BmdClient::tick);
    }

    private static void tick(Minecraft mc) {
        Echolocation.tick();
        closeScreensForBlind(mc);

        if (wheelKey.consumeClick() && mc.gui.screen() == null && mc.player != null) {
            mc.gui.setScreen(new EmoteWheel(wheelKey));
        }

        if (itemSignKey.consumeClick() && mc.gui.screen() == null && mc.player != null) {
            if (ClientState.mine == Sense.MUTE) {
                mc.gui.setScreen(new ItemSignScreen());
            } else {
                mc.player.sendOverlayMessage(
                        Component.literal("Tabliczka z przedmiotem to przywilej niemego.")
                                .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    /**
     * Czern z HUD-u nie zakrywa otwartych ekranow - te rysuja sie pozniej. Bez tego
     * slepy otwiera ekwipunek i widzi wszystko.
     *
     * Ekran czatu zostaje otwarty celowo: historia czatu jest elementem HUD, wiec
     * czern ja zakrywa, a samo pole wpisywania rysuje sie nad nia. Slepy moze
     * napisac /bmd, nie czytajac przy tym wiadomosci. Bez tego nie dalo sie
     * wydac zadnej komendy z gry. Pauza i ekran smierci - zeby dalo sie wyjsc i odrodzic.
     */
    private static void closeScreensForBlind(Minecraft mc) {
        if (ClientState.mine != Sense.BLIND) return;
        Screen screen = mc.gui.screen();
        if (screen == null || screen instanceof PauseScreen || screen instanceof DeathScreen
                || screen instanceof DisconnectedScreen || screen instanceof ChatScreen) {
            return;
        }
        mc.gui.setScreen(null);
    }

    public static void sendEmote(int emoteId) {
        ClientPlayNetworking.send(new BmdPayloads.SignalRequest(emoteId, BmdPayloads.NO_ITEM));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Emote e = Emote.byId(emoteId);
            mc.player.sendOverlayMessage(Component.literal("Pokazujesz: " + e.symbol + " " + e.pl)
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void sendItemSign(Identifier itemId) {
        ClientPlayNetworking.send(new BmdPayloads.SignalRequest(-1, itemId));
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendOverlayMessage(Component.literal("Pokazujesz przedmiot: " + itemId.getPath())
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
