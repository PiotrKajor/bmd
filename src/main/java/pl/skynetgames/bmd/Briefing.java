package pl.skynetgames.bmd;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Opis klasy wyswietlany graczowi. Skladany z aktualnego configu, wiec zawsze
 * zgadza sie z tym, co mod faktycznie blokuje - nie z tym, co kiedys blokowal.
 *
 * Same teksty to klucze tlumaczen, wiec kazdy gracz czyta opis w swoim jezyku,
 * mimo ze sklada go serwer.
 */
public final class Briefing {

    public static List<Component> lines(Sense sense) {
        BmdConfig c = BmdConfig.get();
        List<Component> out = new ArrayList<>();

        out.add(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.DARK_GRAY));
        out.add(Component.literal("  ").append(sense.emoji()).append(Component.literal(" "))
                .append(sense.displayName().copy().withStyle(sense.color, ChatFormatting.BOLD)));
        out.add(Component.empty());

        // Ukonczone wyzwanie wylacza wszystko naraz i nie zostawia po sobie zadnego
        // sladu na ekranie - bez tej linijki opis obiecuje kary, ktorych nie ma,
        // a /bmd mode wyglada na zepsute. Wraca po /bmd goal clear.
        if (!BmdState.effectsActive()) {
            out.add(hint("bmd.goal.effects_off"));
            out.add(Component.empty());
        }

        switch (sense) {
            case BLIND -> {
                BlindMode mode = BlindMode.byName(c.blindMode);
                out.add(flavor(Component.translatable("bmd.brief.blind.mode", mode.displayName())));
                switch (mode) {
                    case EASY -> {
                        out.add(can("bmd.brief.blind.easy1"));
                        out.add(cannot("bmd.brief.blind.easy2"));
                    }
                    case NORMAL -> {
                        out.add(can(Component.translatable("bmd.brief.blind.echo", (int) c.blindEchoRange)));
                        out.add(can("bmd.brief.blind.echo2"));
                        out.add(can("bmd.brief.blind.echo3"));
                    }
                    case HARD -> out.add(cannot("bmd.brief.blind.hard"));
                }
                out.add(can("bmd.brief.blind.hears"));
                out.add(can("bmd.brief.blind.speaks"));
                out.add(c.blindShowHud ? can("bmd.brief.blind.hud") : cannot("bmd.brief.blind.nohud"));
                out.add(cannot("bmd.brief.blind.noworld"));
                if (c.onlyBlindCanCraft) out.add(can("bmd.brief.blind.craft"));
                if (c.blindSlowness) out.add(cannot("bmd.brief.blind.slow"));
                out.add(hint("bmd.brief.blind.hint"));
            }
            case MUTE -> {
                out.add(flavor(Component.translatable("bmd.brief.mute.flavor")));
                out.add(cannot("bmd.brief.mute.novoice"));
                if (c.muteCannotChat) out.add(cannot("bmd.brief.mute.nochat"));
                if (c.muteCannotAttack) out.add(cannot("bmd.brief.mute.noattack"));
                if (c.muteHalfDamage) out.add(cannot("bmd.brief.mute.halfdmg"));
                if (c.muteCannotOpenContainers) out.add(cannot("bmd.brief.mute.nocontainers"));
                if (c.onlyBlindCanCraft) out.add(cannot("bmd.brief.mute.nocraft"));
                out.add(can("bmd.brief.mute.sees"));
                out.add(can("bmd.brief.mute.wheel"));
                if (c.muteItemSign) out.add(can("bmd.brief.mute.sign"));
                out.add(hint("bmd.brief.mute.hint"));
            }
            case DEAF -> {
                out.add(flavor(Component.translatable("bmd.brief.deaf.flavor")));
                out.add(cannot("bmd.brief.deaf.novoice"));
                out.add(cannot("bmd.brief.deaf.nogame"));
                if (c.deafCannotUseItems) {
                    out.add(cannot("bmd.brief.deaf.noplace"));
                    out.add(can("bmd.brief.deaf.caneat"));
                }
                if (c.onlyBlindCanCraft) out.add(cannot("bmd.brief.deaf.nocraft"));
                if (c.deafMinesSlower) out.add(cannot("bmd.brief.deaf.slowmine"));
                if (c.deafHidesNameTags) out.add(cannot("bmd.brief.deaf.nonames"));
                if (c.deafAggroRangeDoubled) out.add(cannot("bmd.brief.deaf.aggro"));
                out.add(can("bmd.brief.deaf.speaks"));
                out.add(can("bmd.brief.deaf.wheel"));
                out.add(hint("bmd.brief.deaf.hint"));
            }
            case NONE -> {
                out.add(flavor(Component.translatable("bmd.brief.none.flavor")));
                out.add(hint("bmd.brief.none.hint"));
            }
        }

        out.add(Component.literal("═══════════════════════════════").withStyle(ChatFormatting.DARK_GRAY));
        return out;
    }

    private static MutableComponent can(String key) {
        return can(Component.translatable(key));
    }

    private static MutableComponent can(Component text) {
        return Component.literal(" ✔ ").withStyle(ChatFormatting.GREEN)
                .append(text.copy().withStyle(ChatFormatting.WHITE));
    }

    private static MutableComponent cannot(String key) {
        return Component.literal(" ✕ ").withStyle(ChatFormatting.RED)
                .append(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent flavor(Component text) {
        return Component.literal("  ").append(text.copy()
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA));
    }

    private static MutableComponent hint(String key) {
        return Component.literal(" ➜ ").withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(key).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
    }

    private Briefing() {
    }
}
