package dev.kajor.bmd;

import net.minecraft.network.chat.Component;

/** Jak mocno slepy jest slepy. */
public enum BlindMode {
    /** Wanilkowe Blindness + Darkness - widac zarys tuz przy twarzy, da sie grac. */
    EASY(),
    /** Pelna czern plus echolokacja dzwiekow. */
    NORMAL(),
    /** Pelna czern, zero podpowiedzi. */
    HARD();

    /** Klucz tlumaczenia nazwy trybu. */
    public String key() {
        return "bmd.mode." + name().toLowerCase();
    }

    public Component displayName() {
        return Component.translatable(key());
    }

    public static BlindMode byName(String s) {
        for (BlindMode m : values()) {
            if (m.name().equalsIgnoreCase(s)) return m;
        }
        return NORMAL;
    }

    public static BlindMode byOrdinal(int i) {
        BlindMode[] all = values();
        return (i >= 0 && i < all.length) ? all[i] : NORMAL;
    }
}
