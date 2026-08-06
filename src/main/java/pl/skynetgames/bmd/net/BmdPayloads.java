package pl.skynetgames.bmd.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pl.skynetgames.bmd.BmdMod;
import pl.skynetgames.bmd.Sense;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Przedmiot na tabliczce leci przez siec jako samo ID z rejestru, nie jako ItemStack.
 * Klient nie moze wiec przemycic spreparowanego NBT - serwer sklada czysty stack sam.
 */
public final class BmdPayloads {

    /** Identifier pustego przedmiotu = brak tabliczki. */
    public static final Identifier NO_ITEM = Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "none");

    /**
     * Serwer -> klient: kto ma jaka klase (do ikon nad glowa), moja wlasna klasa
     * i czy leci tryb hard. Wysylane w calosci przy kazdej zmianie - lista graczy
     * na jednym serwerze jest mala, delty nie sa tego warte.
     */
    public record Roster(Map<UUID, Sense> senses, Sense mine, boolean hardMode, double echoRange) implements CustomPacketPayload {
        public static final Type<Roster> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "roster"));

        public static final StreamCodec<FriendlyByteBuf, Roster> CODEC = StreamCodec.of(
                (buf, v) -> {
                    buf.writeVarInt(v.senses.size());
                    v.senses.forEach((id, s) -> {
                        buf.writeUUID(id);
                        buf.writeVarInt(s.ordinal());
                    });
                    buf.writeVarInt(v.mine.ordinal());
                    buf.writeBoolean(v.hardMode);
                    buf.writeDouble(v.echoRange);
                },
                buf -> {
                    int n = buf.readVarInt();
                    Map<UUID, Sense> map = new HashMap<>(n);
                    for (int i = 0; i < n; i++) {
                        map.put(buf.readUUID(), readSense(buf));
                    }
                    return new Roster(map, readSense(buf), buf.readBoolean(), buf.readDouble());
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Klient -> serwer: chce pokazac gest albo tabliczke z przedmiotem.
     * emoteId < 0 oznacza tabliczke - wtedy liczy sie itemId.
     */
    public record SignalRequest(int emoteId, Identifier itemId) implements CustomPacketPayload {
        public static final Type<SignalRequest> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "signal_request"));

        public static final StreamCodec<FriendlyByteBuf, SignalRequest> CODEC = StreamCodec.of(
                (buf, v) -> {
                    buf.writeVarInt(v.emoteId);
                    buf.writeUtf(v.itemId.toString(), 256);
                },
                buf -> {
                    int id = buf.readVarInt();
                    Identifier item = Identifier.tryParse(buf.readUtf(256));
                    return new SignalRequest(id, item == null ? NO_ITEM : item);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Serwer -> klienci w poblizu: gracz X pokazuje gest / przedmiot. */
    public record Signal(UUID sender, int emoteId, Identifier itemId) implements CustomPacketPayload {
        public static final Type<Signal> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "signal"));

        public static final StreamCodec<FriendlyByteBuf, Signal> CODEC = StreamCodec.of(
                (buf, v) -> {
                    buf.writeUUID(v.sender);
                    buf.writeVarInt(v.emoteId);
                    buf.writeUtf(v.itemId.toString(), 256);
                },
                buf -> {
                    UUID sender = buf.readUUID();
                    int id = buf.readVarInt();
                    Identifier item = Identifier.tryParse(buf.readUtf(256));
                    return new Signal(sender, id, item == null ? NO_ITEM : item);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static Sense readSense(FriendlyByteBuf buf) {
        int i = buf.readVarInt();
        Sense[] all = Sense.values();
        return (i >= 0 && i < all.length) ? all[i] : Sense.NONE;
    }

    private BmdPayloads() {
    }
}
