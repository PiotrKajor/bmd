package dev.kajor.bmd.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.kajor.bmd.BmdConfig;
import dev.kajor.bmd.Debuffs;

/**
 * Edytor ksiazki otwiera sam klient (BmdClient go nie wpuszcza), wiec tu trafia tylko
 * klient, ktory to ominal. Obie metody chodza juz na watku serwera, po filtrze tekstu.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinBookEdit {

    @Shadow
    public ServerPlayer player;

    @Inject(method = {"updateBookContents", "signBook"}, at = @At("HEAD"), cancellable = true)
    private void bmd$noWriting(CallbackInfo ci) {
        if (Debuffs.restricted(player, BmdConfig.get().everyoneCannotWriteBooks)) {
            Debuffs.warn(player, "bmd.warn.no_books");
            ci.cancel();
        }
    }
}
