package dev.kajor.bmd;

/** Jak mocno slepy jest slepy. */
public enum BlindMode {
    /** Wanilkowe Blindness + Darkness - widac zarys tuz przy twarzy, da sie grac. */
    EASY("latwy", "Blindness + Darkness, widzisz tuz przed soba"),
    /** Pelna czern plus echolokacja dzwiekow. */
    NORMAL("normalny", "czern + echolokacja dzwiekow"),
    /** Pelna czern, zero podpowiedzi. */
    HARD("trudny", "czern absolutna, sam dzwiek z gry");

    public final String pl;
    public final String opis;

    BlindMode(String pl, String opis) {
        this.pl = pl;
        this.opis = opis;
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
