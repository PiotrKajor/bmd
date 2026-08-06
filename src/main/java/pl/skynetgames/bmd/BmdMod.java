package pl.skynetgames.bmd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skynetgames.bmd.net.BmdPayloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BmdMod implements ModInitializer {
    public static final String MOD_ID = "bmd";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    /** Jak daleko widac gest nad glowa. */
    private static final double SIGNAL_RANGE = 48.0D;
    private static final long SIGNAL_COOLDOWN_MS = 700L;

    private static final Map<UUID, Long> lastSignal = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(BmdPayloads.Roster.TYPE, BmdPayloads.Roster.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BmdPayloads.Signal.TYPE, BmdPayloads.Signal.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BmdPayloads.SignalRequest.TYPE, BmdPayloads.SignalRequest.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(BmdState::load);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> BmdCommand.register(dispatcher));
        Debuffs.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            BmdSync.broadcast(server);
            if (BmdState.get(handler.getPlayer()) != Sense.NONE) {
                BmdSync.briefing(handler.getPlayer());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> lastSignal.remove(handler.getPlayer().getUUID()));

        ServerPlayNetworking.registerGlobalReceiver(BmdPayloads.SignalRequest.TYPE,
                (payload, context) -> handleSignal(context.player(), payload));

        LOG.info("Blind Mute Deaf gotowy");
    }

    /**
     * Wszystko, co przyszlo od klienta, jest tu sprawdzane od zera: kto, jak czesto,
     * czy w ogole moze i czy przedmiot istnieje. Klient moze wyslac dowolny pakiet.
     */
    private static void handleSignal(ServerPlayer player, BmdPayloads.SignalRequest req) {
        long now = System.currentTimeMillis();
        Long last = lastSignal.get(player.getUUID());
        if (last != null && now - last < SIGNAL_COOLDOWN_MS) return;
        lastSignal.put(player.getUUID(), now);

        Identifier itemId = BmdPayloads.NO_ITEM;
        int emoteId = req.emoteId();

        if (emoteId < 0) {
            // tabliczka z przedmiotem - przywilej niemego
            if (!BmdConfig.get().muteItemSign || BmdState.get(player) != Sense.MUTE) return;
            Item item = BuiltInRegistries.ITEM.getValue(req.itemId());
            if (item == Items.AIR) return;
            itemId = BuiltInRegistries.ITEM.getKey(item);
        } else {
            if (emoteId >= Emote.values().length) return;
            Emote emote = Emote.byId(emoteId);
            // Gest robi halas - slepy uslyszy, ze cos sie dzieje, ale nie dowie sie co.
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    emote.sound, SoundSource.PLAYERS, 0.8F, emote.pitch);
        }

        BmdPayloads.Signal out = new BmdPayloads.Signal(player.getUUID(), emoteId, itemId);
        for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
            if (other.level() == player.level() && other.distanceToSqr(player) <= SIGNAL_RANGE * SIGNAL_RANGE) {
                ServerPlayNetworking.send(other, out);
            }
        }
    }
}
