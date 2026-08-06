package pl.skynetgames.bmd;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Opis klasy wyswietlany graczowi. Generowany z aktualnego configu, wiec zawsze
 * zgadza sie z tym, co mod faktycznie blokuje - nie z tym, co kiedys blokowal.
 */
public final class Briefing {

    public static List<Component> lines(Sense sense) {
        BmdConfig c = BmdConfig.get();
        List<Component> out = new ArrayList<>();

        out.add(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.DARK_GRAY));
        out.add(Component.literal("  " + sense.icon + " ")
                .withStyle(sense.color)
                .append(Component.literal(sense.pl.toUpperCase()).withStyle(sense.color, ChatFormatting.BOLD)));
        out.add(Component.empty());

        switch (sense) {
            case BLIND -> {
                out.add(flavor("Nie widzisz nic. Ekran jest czarny - na stale."));
                if (!c.blindHardMode) {
                    out.add(can("Echolokacja: dzwieki w promieniu " + (int) c.blindEchoRange
                            + " blokow zapalaja wskaznik kierunku na krawedzi ekranu"));
                    out.add(can("Im blizej zrodlo, tym jasniejszy wskaznik"));
                } else {
                    out.add(cannot("TRYB HARD: echolokacja wylaczona, zostaje sam sluch"));
                }
                out.add(can("Slyszysz wszystko - gre i glos na Simple Voice Chat"));
                out.add(can("Mowisz normalnie - jestes uszami i ustami druzyny"));
                out.add(cannot("Nie widzisz ekwipunku, HUD-u ani craftingu"));
                if (c.blindSlowness) out.add(cannot("Poruszasz sie wolniej (Spowolnienie I)"));
                out.add(hint("Trzymaj sie kogos glosem. Sam nie przejdziesz nawet przez drzwi."));
            }
            case MUTE -> {
                out.add(flavor("Nie wydajesz dzwieku. Mikrofon jest odciety na serwerze."));
                out.add(cannot("Nie mowisz na Simple Voice Chat - nikt cie nie uslyszy"));
                if (c.muteCannotChat) out.add(cannot("Nie piszesz na czacie ani /msg"));
                if (c.muteCannotAttack) out.add(cannot("Nie zadajesz obrazen - zadnych, nikomu"));
                if (c.muteHalfDamage) out.add(cannot("Zadajesz o polowe mniejsze obrazenia"));
                if (c.muteCannotOpenContainers) out.add(cannot("Nie otwierasz skrzyn ani piecow"));
                out.add(can("Widzisz i slyszysz wszystko - jestes oczami druzyny"));
                out.add(can("Kolo gestow [G] - 8 znakow z dzwiekiem, widoczne nad glowa"));
                if (c.muteItemSign) {
                    out.add(can("Tabliczka z przedmiotem [B] - wybierz dowolny przedmiot z gry,"));
                    out.add(can("  zawisnie nad twoja glowa na 10 sekund"));
                }
                out.add(hint("Gest ma dzwiek - slepy uslyszy, ze cos pokazujesz, ale nie co."));
            }
            case DEAF -> {
                out.add(flavor("Cisza absolutna. Zero dzwieku z gry i zero glosu."));
                out.add(cannot("Nie slyszysz nikogo na Simple Voice Chat"));
                out.add(cannot("Nie slyszysz gry: krokow, creepera, dzwonka, muzyki"));
                if (c.deafCannotUseItems) out.add(cannot("Nie stawiasz blokow i nie uzywasz PPM (jedzenie, luk, perly)"));
                if (c.deafMinesSlower) out.add(cannot("Kopiesz dwa razy wolniej"));
                if (c.deafHidesNameTags) out.add(cannot("Nie widzisz nickow nad glowami - rozpoznajesz po skinie"));
                if (c.deafAggroRangeDoubled) out.add(cannot("Moby wykrywaja cie z dwa razy wiekszej odleglosci"));
                out.add(can("Mowisz normalnie - inni cie slysza, ty ich nie"));
                out.add(can("Kolo gestow [G] dziala tak samo u ciebie"));
                out.add(hint("Patrz na ikony nad glowami. To twoj jedyny kanal odbiorczy."));
            }
            case NONE -> {
                out.add(flavor("Masz wszystkie zmysly. Nie masz zadnych ograniczen."));
                out.add(hint("/bmd losuj przydzieli klasy graczom."));
            }
        }

        out.add(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.DARK_GRAY));
        return out;
    }

    private static MutableComponent can(String s) {
        return Component.literal(" ✔ ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(s).withStyle(ChatFormatting.WHITE));
    }

    private static MutableComponent cannot(String s) {
        return Component.literal(" ✕ ").withStyle(ChatFormatting.RED)
                .append(Component.literal(s).withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent flavor(String s) {
        return Component.literal("  " + s).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA);
    }

    private static MutableComponent hint(String s) {
        return Component.literal(" ➜ ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(s).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
    }

    private Briefing() {
    }
}
