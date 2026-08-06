package pl.skynetgames.bmd.client;

import net.minecraft.resources.Identifier;
import pl.skynetgames.bmd.Sense;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Co klient wie o trybie. Zerowane przy wyjsciu z serwera. */
public final class ClientState {

    public static Sense mine = Sense.NONE;
    public static boolean hardMode = false;
    /** Zasieg echolokacji - przychodzi z serwera, zeby klient nie zgadywal. */
    public static double echoRange = 24.0D;
    public static final Map<UUID, Sense> ROSTER = new HashMap<>();

    /** Sygnaly wiszace nad glowami: gracz -> co i do kiedy. */
    public static final Map<UUID, Signal> SIGNALS = new HashMap<>();

    /** Zrodla dzwieku dla echolokacji slepego. */
    public static final java.util.List<Echo> ECHOES = new java.util.ArrayList<>();

    public record Signal(int emoteId, Identifier itemId, long expiresAt) {
    }

    public record Echo(double x, double y, double z, long bornAt, long expiresAt) {
    }

    public static Sense senseOf(UUID player) {
        return ROSTER.getOrDefault(player, Sense.NONE);
    }

    public static void reset() {
        mine = Sense.NONE;
        hardMode = false;
        ROSTER.clear();
        SIGNALS.clear();
        ECHOES.clear();
    }

    private ClientState() {
    }
}
