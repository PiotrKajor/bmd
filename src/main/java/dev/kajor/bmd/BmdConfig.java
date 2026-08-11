package dev.kajor.bmd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Config w JSON obok folderu swiata. Flagi debuffow - domyslnie wlaczone tylko te
 * wybrane przy projektowaniu trybu, reszta czeka wylaczona.
 */
public class BmdConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BmdConfig instance;
    private static Path file;

    // --- MUTE ---
    public boolean muteCannotAttack = true;
    public boolean muteCannotChat = true;
    public boolean muteCannotOpenContainers = false;
    public boolean muteHalfDamage = false;

    // --- DEAF ---
    public boolean deafCannotUseItems = true;
    public boolean deafHidesNameTags = false;
    public boolean deafMinesSlower = false;
    public boolean deafAggroRangeDoubled = false;

    // --- BLIND ---
    /** easy = Blindness+Darkness, normal = czern+echolokacja, hard = sama czern. */
    public String blindMode = "NORMAL";
    /** Zasieg (w blokach), z ktorego echolokacja pokazuje kierunek dzwieku. */
    public double blindEchoRange = 24.0D;
    public boolean blindSlowness = false;
    /** Tryb latwy: dodatkowe przyciemnienie ekranu 0.0-1.0 na wierzchu wanilkowych efektow. */
    public double blindEasyDarkness = 0.6D;
    /** Slepy ma nie widziec swiata, ale swoj HUD (hotbar, zycie, glod) juz tak. */
    public boolean blindShowHud = true;

    /** Czy niemy moze wystawic nad glowa dowolny przedmiot jako komunikat. */
    public boolean muteItemSign = true;

    /** Craftowac moze tylko slepy - pozostali musza mu przynosic surowce. */
    public boolean onlyBlindCanCraft = true;

    /** Wyrzucac graczy bez moda po stronie klienta. Patrz ModCheck - bez moda tryb jest do polowy fikcja. */
    public boolean requireClientMod = true;

    public static BmdConfig get() {
        if (instance == null) instance = new BmdConfig();
        return instance;
    }

    public static void load(Path path) {
        file = path;
        try {
            if (Files.exists(path)) {
                instance = GSON.fromJson(Files.readString(path), BmdConfig.class);
            }
            if (instance == null) instance = new BmdConfig();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(instance));
        } catch (IOException | RuntimeException e) {
            BmdMod.LOG.error("Nie udalo sie wczytac configu, jade na domyslnym", e);
            instance = new BmdConfig();
        }
    }

    /** Po zmianie z komendy - bez tego /bmd mode zyje tylko do restartu serwera. */
    public static void save() {
        if (file == null || instance == null) return;
        try {
            Files.writeString(file, GSON.toJson(instance));
        } catch (IOException e) {
            BmdMod.LOG.error("Nie udalo sie zapisac configu", e);
        }
    }
}
