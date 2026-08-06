package pl.skynetgames.bmd;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Tryb latwy nie zaslania ekranu - daje wanilkowe Blindness i Darkness.
 * Slepy widzi wtedy zarys tuz przed soba, wiec da sie chodzic bez opiekuna.
 *
 * Efekty trzeba odnawiac, bo same wygasaja. Odnawiamy co sekunde z zapasem
 * (5 s), zeby przy chwilowej zadyszce serwera ekran nie mrugnal jasnoscia.
 */
public final class BlindEffects {

    private static final int REFRESH_TICKS = 20;
    private static final int EFFECT_TICKS = 100;
    /** Blindness i Darkness nie skaluja sie z poziomem, ale wyzszy nie szkodzi
     *  i liczy sie, gdyby ktores mody na to reagowaly. Prawdziwe wzmocnienie
     *  robi przyciemnienie ekranu po stronie klienta (blindEasyDarkness). */
    private static final int AMPLIFIER = 2;

    private static int counter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++counter < REFRESH_TICKS) return;
            counter = 0;

            boolean easy = BlindMode.byName(BmdConfig.get().blindMode) == BlindMode.EASY;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (BmdState.get(player) != Sense.BLIND) continue;

                if (easy) {
                    // ambient + bez czasteczek: ekran czysty, bez wirujacych kropek
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_TICKS, AMPLIFIER, true, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, EFFECT_TICKS, AMPLIFIER, true, false, false));
                } else {
                    // w pozostalych trybach czern rysuje klient - wanilkowe efekty tylko by przeszkadzaly
                    player.removeEffect(MobEffects.BLINDNESS);
                    player.removeEffect(MobEffects.DARKNESS);
                }
            }
        });
    }

    private BlindEffects() {
    }
}
