package pl.skynetgames.bmd.client.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.skynetgames.bmd.Emote;
import pl.skynetgames.bmd.Sense;
import pl.skynetgames.bmd.client.ClientState;
import pl.skynetgames.bmd.net.BmdPayloads;

/**
 * Gesty i klasa doklejone do nazwy gracza.
 *
 * Wczesniej babelki rysowal wlasny HUD z reczna projekcja swiat->ekran i to
 * sie nie sprawdzalo: pozycja rozjezdzala sie przy innym FOV, skalowaniu GUI
 * i w widoku z trzeciej osoby. Nametag rozwiazuje to za nas - Minecraft sam
 * ustawia go nad glowa, obraca do kamery, skaluje z odlegloscia i chowa,
 * gdy gracz kucnie albo odejdzie za daleko.
 *
 * Emoji dziala, bo nazwa jest zwyklym Component - font moda dziala tu tak
 * samo jak w czacie.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void bmd$decorateNameTag(Entity entity, CallbackInfoReturnable<Component> cir) {
        if (!(entity instanceof Player player)) return;

        Sense sense = ClientState.senseOf(player.getUUID());
        ClientState.Signal signal = ClientState.SIGNALS.get(player.getUUID());
        if (signal != null && signal.expiresAt() < System.currentTimeMillis()) {
            ClientState.SIGNALS.remove(player.getUUID());
            signal = null;
        }
        if (sense == Sense.NONE && signal == null) return;

        Component original = cir.getReturnValue();
        MutableComponent out = Component.empty();

        if (signal != null) {
            out.append(describe(signal)).append(Component.literal(" "));
        }
        if (sense != Sense.NONE) {
            out.append(sense.emoji()).append(Component.literal(" "));
        }
        out.append(original == null ? Component.empty() : original);

        cir.setReturnValue(out);
    }

    /** Gest jako emoji z podpisem, tabliczka jako nazwa przedmiotu w nawiasach. */
    private static Component describe(ClientState.Signal signal) {
        if (signal.emoteId() >= 0) {
            Emote emote = Emote.byId(signal.emoteId());
            return Component.empty().append(emote.emoji())
                    .append(Component.literal(" " + emote.pl).withStyle(ChatFormatting.YELLOW));
        }

        Identifier id = signal.itemId();
        if (id == null || id.equals(BmdPayloads.NO_ITEM)) return Component.empty();
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == Items.AIR) return Component.empty();

        // Ikona przedmiotu jako glif ze sprite'a atlasu - dziala dla przedmiotow
        // z plaska tekstura (item/...). Bloki maja model 3D i sprite'a nie maja,
        // dlatego nazwa zostaje obok jako pewne zrodlo informacji.
        Component icon = Component.literal(" ").withStyle(Style.EMPTY.withFont(
                new FontDescription.AtlasSprite(TextureAtlas.LOCATION_ITEMS,
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()))));

        return Component.empty().append(icon).append(Component.literal(" "))
                .append(new ItemStack(item).getHoverName().copy().withStyle(ChatFormatting.AQUA));
    }
}
