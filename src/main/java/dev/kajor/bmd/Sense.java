package dev.kajor.bmd;

import net.minecraft.ChatFormatting;

/**
 * Klasa gracza. Kazda odbiera jeden zmysl i dokłada wlasne ograniczenia.
 */
public enum Sense {
    NONE("Widzacy", ChatFormatting.GRAY, "✔"),
    BLIND("Slepy", ChatFormatting.DARK_PURPLE, "✕"),
    MUTE("Niemy", ChatFormatting.GOLD, "♪"),
    DEAF("Gluchy", ChatFormatting.AQUA, "⚠");

    public final String pl;
    public final ChatFormatting color;
    /** Symbol z BMP - vanilla font go wyrenderuje, w przeciwienstwie do emoji. */
    public final String icon;

    Sense(String pl, ChatFormatting color, String icon) {
        this.pl = pl;
        this.color = color;
        this.icon = icon;
    }

    public static Sense byName(String s) {
        for (Sense v : values()) {
            if (v.name().equalsIgnoreCase(s)) return v;
        }
        return NONE;
    }

    /** Trzy losowalne klasy - NONE nie wchodzi do losowania. */
    public static final Sense[] PLAYABLE = { BLIND, MUTE, DEAF };
}
