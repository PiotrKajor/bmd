package dev.kajor.bmd.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.kajor.bmd.Sense;
import dev.kajor.bmd.client.ClientState;

/**
 * F1 chowa HUD, a czern slepego jest czescia HUD - slepy z F1 widzial swiat.
 * Zerujemy flage przed kazda klatka, a nie blokujemy samego klawisza: to lapie tez
 * HUD schowany jeszcze zanim gracz dostal klase, i nie przepuszcza ani jednej klatki.
 */
@Mixin(Hud.class)
public class MixinHud {

    @Shadow
    private boolean isHidden;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void bmd$blindCannotHideHud(GuiGraphicsExtractor gfx, DeltaTracker delta, CallbackInfo ci) {
        if (ClientState.mine == Sense.BLIND && ClientState.effectsActive()) isHidden = false;
    }
}
