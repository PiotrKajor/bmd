package dev.kajor.bmd.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.kajor.bmd.BmdConfig;
import dev.kajor.bmd.BmdState;
import dev.kajor.bmd.Sense;

/**
 * Craftowac moze tylko slepy - reszta nosi mu surowce i mowi, co ma zrobic.
 *
 * Celujemy w Slot, a nie w ResultSlot: ResultSlot nie nadpisuje mayPickup,
 * tylko dziedziczy je ze Slot, wiec mixin na ResultSlot nie mial czego trafic.
 * Sam slot wyniku rozpoznajemy po typie w czasie dzialania.
 *
 * Slot wyniku obsluguje kazdy crafting - i stol rzemieslniczy, i siatke 2x2
 * w ekwipunku - wiec jedno miejsce zamyka obie drogi.
 */
@Mixin(Slot.class)
public class MixinResultSlot {

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void bmd$onlyBlindCrafts(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ResultSlot)) return;
        if (!BmdConfig.get().onlyBlindCanCraft) return;

        Sense sense = BmdState.get(player.getUUID());
        // NONE to gracz bez przydzielonej klasy - jego nie ograniczamy.
        if (sense == Sense.NONE || sense == Sense.BLIND) return;

        cir.setReturnValue(false);
    }
}
