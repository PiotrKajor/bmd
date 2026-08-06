package pl.skynetgames.bmd;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Klasa gracza. Kazda odbiera jeden zmysl i dokłada wlasne ograniczenia.
 */
public enum Sense {
    NONE(ChatFormatting.GRAY, "✔"),
    BLIND(ChatFormatting.DARK_PURPLE, "✕"),
    MUTE(ChatFormatting.GOLD, "♪"),
    DEAF(ChatFormatting.AQUA, "⚠");

    /** Klucz tlumaczenia nazwy klasy. */
    public final String key;
    public final ChatFormatting color;
    /** Symbol z BMP - vanilla font go wyrenderuje, w przeciwienstwie do emoji. */
    public final String icon;

    Sense(ChatFormatting color, String icon) {
        this.key = "bmd.sense." + name().toLowerCase();
        this.color = color;
        this.icon = icon;
    }

    /**
     * Emoji klasy z wlasnego fontu moda - trzy malpki: nie widze, nie mowie,
     * nie slysze. Glify leza za gestami, stad przesuniecie o ich liczbe.
     */
    private static final FontDescription EMOJI_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "emoji"));
    private static final int SENSE_GLYPH_START = 0xE000 + 15;

    public Component emoji() {
        if (this == NONE) return Component.empty();
        // BLIND/MUTE/DEAF maja ordinal 1..3, glify ida po kolei od SENSE_GLYPH_START
        char glyph = (char) (SENSE_GLYPH_START + ordinal() - 1);
        return Component.literal(String.valueOf(glyph)).withStyle(Style.EMPTY.withFont(EMOJI_FONT));
    }

    public Component displayName() {
        return Component.translatable(key);
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
