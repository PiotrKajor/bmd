package pl.skynetgames.bmd;

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
import net.minecraft.world.entity.player.Player;

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
                warn(player, "Niemy nie zadaje obrazen.");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (BmdConfig.get().muteCannotChat && BmdState.get(sender) == Sense.MUTE) {
                warn(sender, "Niemy nie pisze. Uzyj kola gestow [G].");
                return false;
            }
            return true;
        });

        // /msg, /me, /say - inaczej mute obchodzi sie czatem w piec sekund
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            if (!BmdConfig.get().muteCannotChat) return true;
            if (source.getEntity() instanceof ServerPlayer p && BmdState.get(p) == Sense.MUTE) {
                warn(p, "Niemy nie pisze. Uzyj kola gestow [G].");
                return false;
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (BmdConfig.get().deafCannotUseItems && sense(player) == Sense.DEAF) {
                warn(player, "Gluchy nie stawia blokow ani nie uzywa PPM.");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (BmdConfig.get().deafCannotUseItems && sense(player) == Sense.DEAF) {
                warn(player, "Gluchy nie uzywa przedmiotow prawym przyciskiem.");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    private static Sense sense(Player player) {
        return player instanceof ServerPlayer sp ? BmdState.get(sp) : Sense.NONE;
    }

    /** Na pasek nad hotbarem - czat zostaje czysty. */
    private static void warn(Entity entity, String text) {
        if (entity instanceof ServerPlayer p) {
            p.sendSystemMessage(Component.literal("✕ " + text).withStyle(ChatFormatting.RED), true);
        }
    }

    private Debuffs() {
    }
}
