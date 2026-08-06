package dev.kajor.bmd;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Debuffs {

    public static void register() {
        // Blokada obrazen siedzi na ALLOW_DAMAGE, a nie na samym kliknieciu w moba -
        // dzieki temu lapie tez strzaly, trident i wszystko inne, co gracz wyprodukuje.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!BmdConfig.get().muteCannotAttack) return true;
            return !(source.getEntity() instanceof ServerPlayer attacker && BmdState.get(attacker) == Sense.MUTE);
        });

        // Sam ALLOW_DAMAGE nie da graczowi feedbacku - to jest tylko po to, zeby wiedzial dlaczego.
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (BmdConfig.get().muteCannotAttack && sense(player) == Sense.MUTE) {
                warn(player, "bmd.warn.mute_no_attack");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (BmdConfig.get().muteCannotChat && BmdState.get(sender) == Sense.MUTE) {
                warn(sender, "bmd.warn.mute_no_chat");
                return false;
            }
            return true;
        });

        // /msg, /me, /say - inaczej mute obchodzi sie czatem w piec sekund
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            if (!BmdConfig.get().muteCannotChat) return true;
            if (source.getEntity() instanceof ServerPlayer p && BmdState.get(p) == Sense.MUTE) {
                warn(p, "bmd.warn.mute_no_chat");
                return false;
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (BmdConfig.get().deafCannotUseItems && sense(player) == Sense.DEAF) {
                warn(player, "bmd.warn.deaf_no_place");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (BmdConfig.get().deafCannotUseItems && sense(player) == Sense.DEAF) {
                // Glod nie jest czescia kary - blokada PPM nie moze oznaczac smierci glodowej.
                if (isFood(player.getItemInHand(hand))) return InteractionResult.PASS;
                warn(player, "bmd.warn.deaf_no_use");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    /** Cokolwiek da sie zjesc albo wypic - liczy sie komponent, nie lista przedmiotow. */
    private static boolean isFood(ItemStack stack) {
        return stack.get(DataComponents.FOOD) != null || stack.get(DataComponents.CONSUMABLE) != null;
    }

    private static Sense sense(Player player) {
        return player instanceof ServerPlayer sp ? BmdState.get(sp) : Sense.NONE;
    }

    /** Na pasek nad hotbarem - czat zostaje czysty. */
    private static void warn(Entity entity, String key) {
        if (entity instanceof ServerPlayer p) {
            p.sendSystemMessage(Component.literal("✕ ").append(Component.translatable(key))
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    private Debuffs() {
    }
}
