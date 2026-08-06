package pl.skynetgames.bmd;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Slownik gestow z kola. Symbole sa z BMP - vanilla font je rysuje,
 * emoji z zakresu U+1F600 nie - dlatego mod ma wlasny font (emoji()).
 * Kazdy gest ma dzwiek: niewidomy go uslyszy i zobaczy w echolokacji,
 * gluchy nie uslyszy nic i musi patrzec na ikone.
 *
 * Pietnascie pozycji - sektor ma 24 stopnie, wciaz szeroko na tyle, zeby
 * trafic w niego bez celowania.
 */
public enum Emote {
    // --- podstawy rozmowy ---
    YES("✔", SoundEvents.NOTE_BLOCK_BELL, 1.6F),
    NO("✕", SoundEvents.NOTE_BLOCK_BASS, 0.7F),
    DUNNO("?", SoundEvents.NOTE_BLOCK_SNARE, 1.0F),
    LAUGH("☺", SoundEvents.NOTE_BLOCK_XYLOPHONE, 1.4F),

    // --- alarmy ---
    HELP("❤", SoundEvents.NOTE_BLOCK_PLING, 2.0F),
    DANGER("⚠", SoundEvents.NOTE_BLOCK_DIDGERIDOO, 0.6F),
    ENEMY("⚔", SoundEvents.NOTE_BLOCK_BASS, 1.9F),
    DYING("☠", SoundEvents.NOTE_BLOCK_BASS, 0.5F),

    // --- kierowanie druzyna ---
    FOLLOW("➜", SoundEvents.NOTE_BLOCK_FLUTE, 1.2F),
    WAIT("⌛", SoundEvents.NOTE_BLOCK_HAT, 1.0F),
    HERE("⚑", SoundEvents.NOTE_BLOCK_BELL, 1.0F),
    HOME("⌂", SoundEvents.NOTE_BLOCK_FLUTE, 0.8F),

    // --- towarzyskie ---
    HELLO("✋", SoundEvents.NOTE_BLOCK_CHIME, 1.5F),
    CLAP("✷", SoundEvents.NOTE_BLOCK_BANJO, 1.3F),
    RUDE("☝", SoundEvents.NOTE_BLOCK_DIDGERIDOO, 1.8F);

    /** Klucz tlumaczenia nazwy gestu. */
    public final String key;
    public final String symbol;
    public final Holder<SoundEvent> sound;
    public final float pitch;

    Emote(String symbol, Holder<SoundEvent> sound, float pitch) {
        this.key = "bmd.emote." + name().toLowerCase();
        this.symbol = symbol;
        this.sound = sound;
        this.pitch = pitch;
    }

    /** Wlasny font moda z prawdziwymi emoji - patrz tools/build_emoji_font.py. */
    private static final FontDescription EMOJI_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(BmdMod.MOD_ID, "emoji"));
    /** Glify siedza w Private Use Area, po kolei od U+E000 - indeks gestu daje znak. */
    private static final int PUA_START = 0xE000;

    /**
     * Emoji jako gotowy Component. Znak sam w sobie nic nie znaczy - dopiero
     * przypisany font moda zamienia go na obrazek. Klient bez moda zobaczylby
     * pusty kwadrat, ale takiego serwer i tak nie wpuszcza (ModCheck).
     */
    public Component emoji() {
        return Component.literal(String.valueOf((char) (PUA_START + ordinal())))
                .withStyle(Style.EMPTY.withFont(EMOJI_FONT));
    }

    public Component displayName() {
        return Component.translatable(key);
    }

    public static Emote byId(int id) {
        Emote[] v = values();
        return (id >= 0 && id < v.length) ? v[id] : DUNNO;
    }
}
