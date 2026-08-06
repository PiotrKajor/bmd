package dev.kajor.bmd;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Slownik gestow z kola. Symbole sa z BMP - vanilla font je rysuje,
 * emoji z zakresu U+1F600 nie (dlatego ikona to dodatkowo przedmiot).
 * Kazdy gest ma dzwiek: niewidomy go uslyszy i zobaczy w echolokacji,
 * gluchy nie uslyszy nic i musi patrzec na ikone.
 */
public enum Emote {
    // 26.x: kolorowe warianty siedza w ColorCollection, nie w osobnych polach
    YES("Tak", "✔", Items.DYE.pick(DyeColor.LIME), SoundEvents.NOTE_BLOCK_BELL, 1.6F),
    NO("Nie", "✕", Items.DYE.pick(DyeColor.RED), SoundEvents.NOTE_BLOCK_BASS, 0.7F),
    HELP("Pomocy!", "❤", Items.TOTEM_OF_UNDYING, SoundEvents.NOTE_BLOCK_PLING, 2.0F),
    DANGER("Uwaga!", "⚠", Items.TNT, SoundEvents.NOTE_BLOCK_DIDGERIDOO, 0.6F),
    FOLLOW("Za mna", "➜", Items.COMPASS, SoundEvents.NOTE_BLOCK_FLUTE, 1.2F),
    WAIT("Czekaj", "⌛", Items.CLOCK, SoundEvents.NOTE_BLOCK_HAT, 1.0F),
    DUNNO("Nie wiem", "?", Items.BOOK, SoundEvents.NOTE_BLOCK_SNARE, 1.0F),
    LAUGH("Ha ha!", "☺", Items.CAKE, SoundEvents.NOTE_BLOCK_XYLOPHONE, 1.4F);

    public final String pl;
    public final String symbol;
    public final Item icon;
    public final Holder<SoundEvent> sound;
    public final float pitch;

    Emote(String pl, String symbol, Item icon, Holder<SoundEvent> sound, float pitch) {
        this.pl = pl;
        this.symbol = symbol;
        this.icon = icon;
        this.sound = sound;
        this.pitch = pitch;
    }

    public static Emote byId(int id) {
        Emote[] v = values();
        return (id >= 0 && id < v.length) ? v[id] : DUNNO;
    }
}
