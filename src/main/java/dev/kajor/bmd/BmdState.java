package dev.kajor.bmd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kto ma jaka klase. Trzymane w JSON obok swiata - prostsze i odporniejsze
 * niz SavedData, ktore zmienia API co kilka wersji gry.
 */
public class BmdState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();

    private static final Map<UUID, Sense> SENSES = new HashMap<>();
    private static Path file;

    public static void load(MinecraftServer server) {
        file = server.getWorldPath(LevelResource.ROOT).resolve("bmd_state.json");
        BmdConfig.load(server.getWorldPath(LevelResource.ROOT).resolve("bmd_config.json"));
        SENSES.clear();
        try {
            if (Files.exists(file)) {
                Map<String, String> raw = GSON.fromJson(Files.readString(file), MAP_TYPE);
                if (raw != null) {
                    raw.forEach((k, v) -> SENSES.put(UUID.fromString(k), Sense.byName(v)));
                }
            }
        } catch (IOException | RuntimeException e) {
            BmdMod.LOG.error("Nie udalo sie wczytac stanu BMD", e);
        }
    }

    private static void save() {
        if (file == null) return;
        Map<String, String> raw = new HashMap<>();
        SENSES.forEach((k, v) -> raw.put(k.toString(), v.name()));
        try {
            Files.writeString(file, GSON.toJson(raw));
        } catch (IOException e) {
            BmdMod.LOG.error("Nie udalo sie zapisac stanu BMD", e);
        }
    }

    public static Sense get(UUID player) {
        return SENSES.getOrDefault(player, Sense.NONE);
    }

    public static Sense get(ServerPlayer player) {
        return get(player.getUUID());
    }

    public static void set(UUID player, Sense sense) {
        if (sense == Sense.NONE) SENSES.remove(player);
        else SENSES.put(player, sense);
        save();
    }

    public static void clearAll() {
        SENSES.clear();
        save();
    }

    public static Map<UUID, Sense> all() {
        return SENSES;
    }
}
