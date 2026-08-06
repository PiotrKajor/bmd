package dev.kajor.bmd.goal;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import dev.kajor.bmd.BmdMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stan wyzwania: jaki cel, od kiedy leci i czy juz padl.
 *
 * Postep zabijania liczymy per gracz, ale cel jest wspolny dla calego serwera -
 * to wyzwanie dla druzyny, wiec licznik tez jest druzynowy.
 */
public final class GoalState {

    private static Goal goal;
    private static long startedAt;
    private static long finishedAt;
    private static UUID finishedBy;
    private static final Map<String, Integer> KILLS = new HashMap<>();

    private static Path file;

    public static void load(MinecraftServer server) {
        file = server.getWorldPath(LevelResource.ROOT).resolve("bmd_goal.json");
        goal = null;
        startedAt = finishedAt = 0L;
        finishedBy = null;
        KILLS.clear();
        try {
            if (Files.exists(file)) {
                String raw = Files.readString(file).trim();
                // Prosty format klucz=wartosc - jeden cel i kilka liczb nie potrzebuje JSON-a.
                for (String line : raw.split("\n")) {
                    String[] kv = line.split("=", 2);
                    if (kv.length != 2) continue;
                    switch (kv[0]) {
                        case "goal" -> goal = Goal.byId(kv[1]);
                        case "startedAt" -> startedAt = Long.parseLong(kv[1]);
                        case "finishedAt" -> finishedAt = Long.parseLong(kv[1]);
                        case "finishedBy" -> finishedBy = kv[1].isEmpty() ? null : UUID.fromString(kv[1]);
                        default -> {
                            if (kv[0].startsWith("kill:")) {
                                KILLS.put(kv[0].substring(5), Integer.parseInt(kv[1]));
                            }
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            BmdMod.LOG.error("Nie udalo sie wczytac stanu celu", e);
        }
    }

    private static void save() {
        if (file == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("goal=").append(goal == null ? "" : goal.id()).append('\n');
        sb.append("startedAt=").append(startedAt).append('\n');
        sb.append("finishedAt=").append(finishedAt).append('\n');
        sb.append("finishedBy=").append(finishedBy == null ? "" : finishedBy).append('\n');
        KILLS.forEach((k, v) -> sb.append("kill:").append(k).append('=').append(v).append('\n'));
        try {
            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            BmdMod.LOG.error("Nie udalo sie zapisac stanu celu", e);
        }
    }

    public static void start(Goal newGoal) {
        goal = newGoal;
        startedAt = System.currentTimeMillis();
        finishedAt = 0L;
        finishedBy = null;
        KILLS.clear();
        save();
    }

    public static void clear() {
        goal = null;
        startedAt = finishedAt = 0L;
        finishedBy = null;
        KILLS.clear();
        save();
    }

    public static void finish(UUID by) {
        if (goal == null || finishedAt != 0L) return;
        finishedAt = System.currentTimeMillis();
        finishedBy = by;
        save();
    }

    public static int addKill(String entityId) {
        int n = KILLS.merge(entityId, 1, Integer::sum);
        save();
        return n;
    }

    public static int kills(String entityId) {
        return KILLS.getOrDefault(entityId, 0);
    }

    public static Goal goal() {
        return goal;
    }

    public static boolean isRunning() {
        return goal != null && finishedAt == 0L;
    }

    public static boolean isFinished() {
        return goal != null && finishedAt != 0L;
    }

    public static long startedAt() {
        return startedAt;
    }

    /** Czas w milisekundach: biezacy przy trwajacym wyzwaniu, koncowy po zakonczeniu. */
    public static long elapsedMs() {
        if (goal == null || startedAt == 0L) return 0L;
        long end = finishedAt != 0L ? finishedAt : System.currentTimeMillis();
        return end - startedAt;
    }

    public static UUID finishedBy() {
        return finishedBy;
    }

    private GoalState() {
    }
}
