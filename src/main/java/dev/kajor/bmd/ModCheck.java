package dev.kajor.bmd;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import dev.kajor.bmd.net.BmdPayloads;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Fabric - w odroznieniu od NeoForge - nie sprawdza zgodnosci modow przy wejsciu,
 * a ten mod nie rejestruje zadnych blokow ani przedmiotow. Gracz bez moda wszedlby
 * wiec bez zadnego bledu i po cichu grał w polowe trybu: "slepy" widzialby normalnie,
 * "gluchy" slyszalby cala gre. To nie jest niedogodnosc, to jest oszustwo - stad ta blokada.
 *
 * Wykrywanie po kanale sieciowym: klient z modem rejestruje kanal bmd:roster.
 */
public final class ModCheck {

    /** Kanaly potrafia dojsc chwile po JOIN - bez tej karencji lecialyby falszywe wyrzuty. */
    private static final long GRACE_MS = 4000L;

    private static final Map<UUID, Long> pending = new HashMap<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!BmdConfig.get().requireClientMod) return;
            ServerPlayer player = handler.getPlayer();
            if (!hasMod(player)) {
                pending.put(player.getUUID(), System.currentTimeMillis() + GRACE_MS);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                pending.remove(handler.getPlayer().getUUID()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (pending.isEmpty()) return;
            long now = System.currentTimeMillis();

            Iterator<Map.Entry<UUID, Long>> it = pending.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

                if (player == null) {
                    it.remove();
                } else if (hasMod(player)) {
                    it.remove();
                } else if (now >= entry.getValue()) {
                    it.remove();
                    kick(player);
                }
            }
        });
    }

    private static boolean hasMod(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, BmdPayloads.Roster.TYPE);
    }

    private static void kick(ServerPlayer player) {
        BmdMod.LOG.info("Wyrzucono {} - brak moda Blind Mute Deaf po stronie klienta",
                player.getGameProfile().name());
        player.connection.disconnect(Component.empty()
                .append(Component.translatable("bmd.kick.no_mod")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal("\n\n"))
                .append(Component.translatable("bmd.kick.no_mod_why").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("\n\n"))
                .append(Component.translatable("bmd.kick.no_mod_how").withStyle(ChatFormatting.WHITE)));
    }

    private ModCheck() {
    }
}
