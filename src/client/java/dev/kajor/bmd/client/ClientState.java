package dev.kajor.bmd.client;

import net.minecraft.resources.Identifier;
import dev.kajor.bmd.BlindMode;
import dev.kajor.bmd.Sense;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Co klient wie o trybie. Zerowane przy wyjsciu z serwera. */
public final class ClientState {

    public static Sense mine = Sense.NONE;
    public static BlindMode blindMode = BlindMode.NORMAL;
    /** Zasieg echolokacji - przychodzi z serwera, zeby klient nie zgadywal. */
    public static double echoRange = 24.0D;
    /** Czy czern ma isc pod HUD (hotbar i paski widoczne). */
    public static boolean showHud = true;
    /** Dodatkowe przyciemnienie ekranu w trybie latwym (0.0-1.0). */
    public static double easyDarkness = 0.6D;
    public static final Map<UUID, Sense> ROSTER = new HashMap<>();

    /** Aktywne wyzwanie - null, gdy zadnego nie ma. */
    public static String goalId = null;
    public static long goalStartedAt = 0L;
    /** > 0 oznacza cel ukonczony i czas koncowy. */
    public static long goalFinishedMs = 0L;

    /** Sygnaly wiszace nad glowami: gracz -> co i do kiedy. */
    public static final Map<UUID, Signal> SIGNALS = new HashMap<>();

    /** Zrodla dzwieku dla echolokacji slepego. */
    public static final java.util.List<Echo> ECHOES = new java.util.ArrayList<>();

    public record Signal(int emoteId, Identifier itemId, long expiresAt) {
    }

    public record Echo(double x, double y, double z, long bornAt, long expiresAt) {
    }

    /**
     * Czy ograniczenia maja dzialac - odpowiednik BmdState.effectsActive() na serwerze.
     * Nie potrzebuje osobnego pola: ukonczone wyzwanie ma ustawiony czas koncowy.
     */
    public static boolean effectsActive() {
        return goalFinishedMs == 0L;
    }

    public static Sense senseOf(UUID player) {
        return ROSTER.getOrDefault(player, Sense.NONE);
    }

    public static void reset() {
        mine = Sense.NONE;
        blindMode = BlindMode.NORMAL;
        showHud = true;
        easyDarkness = 0.6D;
        ROSTER.clear();
        SIGNALS.clear();
        ECHOES.clear();
    }

    private ClientState() {
    }
}
