package dev.kajor.bmd;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import dev.kajor.bmd.net.BmdPayloads;

public final class BmdSync {

    /** Kazdy dostaje pelna liste klas + swoja wlasna. */
    public static void broadcast(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            send(p);
        }
    }

    public static void send(ServerPlayer player) {
        ServerPlayNetworking.send(player, new BmdPayloads.Roster(
                BmdState.all(), BmdState.get(player), BmdConfig.get().blindHardMode, BmdConfig.get().blindEchoRange));
    }

    /** Wyswietla graczowi opis jego klasy. */
    public static void briefing(ServerPlayer player) {
        for (Component line : Briefing.lines(BmdState.get(player))) {
            player.sendSystemMessage(line);
        }
    }

    private BmdSync() {
    }
}
