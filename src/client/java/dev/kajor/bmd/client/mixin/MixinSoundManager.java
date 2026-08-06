package dev.kajor.bmd.client.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.kajor.bmd.BlindMode;
import dev.kajor.bmd.Sense;
import dev.kajor.bmd.client.ClientState;
import dev.kajor.bmd.client.Echolocation;

/**
 * Jedno miejsce, przez ktore przechodzi kazdy dzwiek gry - dlatego lapiemy tu oba
 * przypadki naraz: gluchemu dzwiek nie zostaje odtworzony, slepemu zostaje zamieniony
 * na wskaznik kierunku.
 *
 * Glos z Simple Voice Chat NIE idzie tedy (SVC ma wlasny OpenAL), i dobrze -
 * wyciszaniem glosu zajmuje sie serwer, ktorego nie da sie oszukac.
 */
@Mixin(SoundManager.class)
public class MixinSoundManager {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At("HEAD"), cancellable = true)
    private void bmd$filterSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (ClientState.mine == Sense.DEAF) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            return;
        }
        if (ClientState.mine == Sense.BLIND && ClientState.blindMode == BlindMode.NORMAL) {
            Echolocation.onSound(sound);
        }
    }

    /** Druga droga do glosnika - bez tego opoznione dzwieki przeciekaja gluchemu. */
    @Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
    private void bmd$filterDelayedSound(SoundInstance sound, int delay, CallbackInfo ci) {
        if (ClientState.mine == Sense.DEAF) {
            ci.cancel();
        }
    }
}
