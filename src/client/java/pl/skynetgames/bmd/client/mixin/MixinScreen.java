package pl.skynetgames.bmd.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.skynetgames.bmd.BlindMode;
import pl.skynetgames.bmd.Sense;
import pl.skynetgames.bmd.client.ClientState;

/**
 * Przy otwartym ekranie Minecraft nie rysuje HUD-u, wiec czern z BlindHud znika
 * i swiat przeswitywalby przez pol-przezroczyste tlo ekwipunku. Dlatego slepemu
 * zaczerniamy tlo kazdego ekranu.
 *
 * Dzieki temu moze normalnie korzystac z ekwipunku i skrzyn - widzi sloty
 * i przedmioty, bo one rysuja sie po tle - ale nie widzi swiata za nimi.
 */
@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void bmd$blackoutBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY,
                                        float partialTick, CallbackInfo ci) {
        if (ClientState.mine != Sense.BLIND) return;
        // W trybie latwym slepote robia wanilkowe efekty - nie zaslaniamy ekranu.
        if (ClientState.blindMode == BlindMode.EASY) return;
        gfx.fill(0, 0, gfx.guiWidth(), gfx.guiHeight(), 0xFF000000);
    }
}
