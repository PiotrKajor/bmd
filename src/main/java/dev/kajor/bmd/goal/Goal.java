package dev.kajor.bmd.goal;

import java.util.ArrayList;
import java.util.List;

/**
 * Cel wyzwania. Sprawdzalny bez zgadywania: albo ktos ma przedmiot, albo cos
 * zabil, albo wszedl do wymiaru. Trzy typy warunkow wystarczaja na wszystkie
 * szescdziesiat celow, a kazdy da sie sprawdzic tanio raz na sekunde.
 *
 * Nazwy sa kluczami tlumaczen (bmd.goal.<id>), nie gotowym tekstem - opis celu
 * wyswietla sie w jezyku gracza.
 */
public record Goal(String id, Difficulty difficulty, Type type, String target, int amount) {

    public enum Difficulty {
        EASY, NORMAL, HARD;

        public static Difficulty byName(String s) {
            for (Difficulty d : values()) {
                if (d.name().equalsIgnoreCase(s)) return d;
            }
            return EASY;
        }
    }

    public enum Type {
        /** Miec przedmiot w ekwipunku (target = id przedmiotu). */
        HAVE,
        /** Zabic tyle sztuk danego mobа (target = id typu encji). */
        KILL,
        /** Wejsc do wymiaru (target = id wymiaru). */
        REACH
    }

    /** Klucz tlumaczenia opisu celu. */
    public String translationKey() {
        return "bmd.goal." + id;
    }

    // ─────────────────────────── lista celow ───────────────────────────

    private static final List<Goal> ALL = new ArrayList<>();

    private static void have(String id, Difficulty d, String item, int n) {
        ALL.add(new Goal(id, d, Type.HAVE, item, n));
    }

    private static void kill(String id, Difficulty d, String entity, int n) {
        ALL.add(new Goal(id, d, Type.KILL, entity, n));
    }

    private static void reach(String id, Difficulty d, String dimension) {
        ALL.add(new Goal(id, d, Type.REACH, dimension, 1));
    }

    static {
        // ── LATWE (20) ──────────────────────────────────────────────────
        have("wood", Difficulty.EASY, "minecraft:oak_log", 16);
        have("planks", Difficulty.EASY, "minecraft:oak_planks", 32);
        have("crafting_table", Difficulty.EASY, "minecraft:crafting_table", 1);
        have("stone_pickaxe", Difficulty.EASY, "minecraft:stone_pickaxe", 1);
        have("furnace", Difficulty.EASY, "minecraft:furnace", 1);
        have("torches", Difficulty.EASY, "minecraft:torch", 16);
        have("bread", Difficulty.EASY, "minecraft:bread", 3);
        have("coal", Difficulty.EASY, "minecraft:coal", 8);
        have("iron_ingot", Difficulty.EASY, "minecraft:iron_ingot", 5);
        have("bed", Difficulty.EASY, "minecraft:white_bed", 1);
        have("chest", Difficulty.EASY, "minecraft:chest", 2);
        have("bucket", Difficulty.EASY, "minecraft:bucket", 1);
        have("wool", Difficulty.EASY, "minecraft:white_wool", 8);
        have("cooked_beef", Difficulty.EASY, "minecraft:cooked_beef", 5);
        have("boat", Difficulty.EASY, "minecraft:oak_boat", 1);
        have("fishing_rod", Difficulty.EASY, "minecraft:fishing_rod", 1);
        have("shield", Difficulty.EASY, "minecraft:shield", 1);
        kill("kill_zombies", Difficulty.EASY, "minecraft:zombie", 5);
        kill("kill_skeletons", Difficulty.EASY, "minecraft:skeleton", 3);
        kill("kill_spiders", Difficulty.EASY, "minecraft:spider", 3);

        // ── NORMALNE (20) ───────────────────────────────────────────────
        have("diamond", Difficulty.NORMAL, "minecraft:diamond", 3);
        have("iron_armor", Difficulty.NORMAL, "minecraft:iron_chestplate", 1);
        have("gold_ingot", Difficulty.NORMAL, "minecraft:gold_ingot", 10);
        have("redstone", Difficulty.NORMAL, "minecraft:redstone", 16);
        have("obsidian", Difficulty.NORMAL, "minecraft:obsidian", 10);
        have("enchanting_table", Difficulty.NORMAL, "minecraft:enchanting_table", 1);
        have("bookshelf", Difficulty.NORMAL, "minecraft:bookshelf", 5);
        have("golden_apple", Difficulty.NORMAL, "minecraft:golden_apple", 1);
        have("ender_pearl", Difficulty.NORMAL, "minecraft:ender_pearl", 4);
        have("lapis", Difficulty.NORMAL, "minecraft:lapis_lazuli", 12);
        have("emerald", Difficulty.NORMAL, "minecraft:emerald", 5);
        have("diamond_pickaxe", Difficulty.NORMAL, "minecraft:diamond_pickaxe", 1);
        have("anvil", Difficulty.NORMAL, "minecraft:anvil", 1);
        have("cake", Difficulty.NORMAL, "minecraft:cake", 1);
        have("map", Difficulty.NORMAL, "minecraft:map", 1);
        have("brewing_stand", Difficulty.NORMAL, "minecraft:brewing_stand", 1);
        reach("reach_nether", Difficulty.NORMAL, "minecraft:the_nether");
        kill("kill_creepers", Difficulty.NORMAL, "minecraft:creeper", 5);
        kill("kill_endermen", Difficulty.NORMAL, "minecraft:enderman", 2);
        kill("kill_witch", Difficulty.NORMAL, "minecraft:witch", 1);

        // ── TRUDNE (20) ─────────────────────────────────────────────────
        have("netherite_ingot", Difficulty.HARD, "minecraft:netherite_ingot", 1);
        have("ancient_debris", Difficulty.HARD, "minecraft:ancient_debris", 3);
        have("diamond_armor_full", Difficulty.HARD, "minecraft:diamond_chestplate", 1);
        have("blaze_rod", Difficulty.HARD, "minecraft:blaze_rod", 6);
        have("ender_eye", Difficulty.HARD, "minecraft:ender_eye", 8);
        have("nether_star", Difficulty.HARD, "minecraft:nether_star", 1);
        have("elytra", Difficulty.HARD, "minecraft:elytra", 1);
        have("shulker_shell", Difficulty.HARD, "minecraft:shulker_shell", 2);
        have("totem", Difficulty.HARD, "minecraft:totem_of_undying", 1);
        have("trident", Difficulty.HARD, "minecraft:trident", 1);
        have("beacon", Difficulty.HARD, "minecraft:beacon", 1);
        have("dragon_egg", Difficulty.HARD, "minecraft:dragon_egg", 1);
        have("golden_apple_ench", Difficulty.HARD, "minecraft:enchanted_golden_apple", 1);
        have("wither_skull", Difficulty.HARD, "minecraft:wither_skeleton_skull", 3);
        reach("reach_end", Difficulty.HARD, "minecraft:the_end");
        kill("kill_blazes", Difficulty.HARD, "minecraft:blaze", 10);
        kill("kill_wither_skeletons", Difficulty.HARD, "minecraft:wither_skeleton", 5);
        kill("kill_piglin_brute", Difficulty.HARD, "minecraft:piglin_brute", 3);
        kill("kill_ravager", Difficulty.HARD, "minecraft:ravager", 1);
        kill("kill_dragon", Difficulty.HARD, "minecraft:ender_dragon", 1);
    }

    public static List<Goal> all() {
        return ALL;
    }

    public static List<Goal> byDifficulty(Difficulty d) {
        return ALL.stream().filter(g -> g.difficulty() == d).toList();
    }

    public static Goal byId(String id) {
        return ALL.stream().filter(g -> g.id().equals(id)).findFirst().orElse(null);
    }

    // --- self-check -------------------------------------------------------

    public static void main(String[] args) {
        check(ALL.size() == 60, "jest 60 celow, a jest " + ALL.size());
        for (Difficulty d : Difficulty.values()) {
            int n = byDifficulty(d).size();
            check(n == 20, d + " ma 20 celow, a ma " + n);
        }
        List<String> ids = ALL.stream().map(Goal::id).toList();
        check(ids.size() == ids.stream().distinct().count(), "identyfikatory sie powtarzaja");
        for (Goal g : ALL) {
            check(g.target().contains(":"), g.id() + ": cel bez przestrzeni nazw");
            check(g.amount() > 0, g.id() + ": ilosc musi byc dodatnia");
            check(byId(g.id()) == g, g.id() + ": nie da sie odszukac po id");
        }
        System.out.println("Goal: 60 celow, po 20 na poziom, identyfikatory unikalne");
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }
}
