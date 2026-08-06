package pl.skynetgames.bmd.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import pl.skynetgames.bmd.BmdMod;
import pl.skynetgames.bmd.Sense;
import pl.skynetgames.bmd.net.BmdPayloads;

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
                }));

        ClientPlayNetworking.registerGlobalReceiver(BmdPayloads.Signal.TYPE, (payload, context) ->
                context.client().execute(() -> ClientState.SIGNALS.put(payload.sender(),
                        new ClientState.Signal(payload.emoteId(), payload.itemId(),
                                System.currentTimeMillis() + SIGNAL_LIFETIME_MS))));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientState.reset());

        // Czern musi byc na samym wierzchu, inaczej slepy czyta swoj ekwipunek.
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "signals"), new SignalHud());
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "blind"), new BlindHud());

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
     * slepy otwiera ekwipunek i widzi wszystko. Menu pauzy i ekran smierci zostaja,
     * inaczej nie dalo by sie ani wyjsc z gry, ani odrodzic.
     */
    private static void closeScreensForBlind(Minecraft mc) {
        if (ClientState.mine != Sense.BLIND) return;
        Screen screen = mc.gui.screen();
        if (screen == null || screen instanceof PauseScreen || screen instanceof DeathScreen
                || screen instanceof DisconnectedScreen) {
            return;
        }
        mc.gui.setScreen(null);
    }

    public static void sendEmote(int emoteId) {
        ClientPlayNetworking.send(new BmdPayloads.SignalRequest(emoteId, BmdPayloads.NO_ITEM));
    }

    public static void sendItemSign(Identifier itemId) {
        ClientPlayNetworking.send(new BmdPayloads.SignalRequest(-1, itemId));
    }
}
