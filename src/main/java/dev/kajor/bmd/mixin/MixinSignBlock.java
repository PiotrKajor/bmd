package dev.kajor.bmd.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.kajor.bmd.BmdConfig;
import dev.kajor.bmd.Debuffs;

/**
 * Jedno waskie gardlo dla tabliczek: tedy idzie i postawienie nowej, i klikniecie w stara.
 * Bez tej metody gracz nie dostaje prawa edycji (setAllowedPlayerEditor), wiec vanilla
 * sama odrzuci pakiet z tekstem od zmodowanego klienta - osobna blokada nie jest potrzebna.
 * Tabliczke da sie dalej postawic, tylko pusta.
 */
@Mixin(SignBlock.class)
public class MixinSignBlock {

    @Inject(method = "openTextEdit", at = @At("HEAD"), cancellable = true)
    private void bmd$noWriting(Player player, SignBlockEntity sign, boolean front, CallbackInfo ci) {
        if (player instanceof ServerPlayer p && Debuffs.restricted(p, BmdConfig.get().everyoneCannotWriteSigns)) {
            Debuffs.warn(p, "bmd.warn.no_signs");
            ci.cancel();
        }
    }
}
