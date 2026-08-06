package dev.kajor.bmd.goal;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Pilnuje, czy cel zostal wykonany.
 *
 * Posiadanie przedmiotu i wejscie do wymiaru sprawdzamy raz na sekunde - czesciej
 * nie ma sensu, a przy kazdym ticku liczylibysmy ekwipunek 20 razy szybciej bez
 * zadnego zysku. Zabicia lapiemy eventem, bo trupa nie da sie znalezc po fakcie.
 */
public final class GoalTracker {

    private static final int CHECK_TICKS = 20;
    private static int counter = 0;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!GoalState.isRunning()) return;
            Goal goal = GoalState.goal();
            if (goal.type() != Goal.Type.KILL) return;
            if (!(source.getEntity() instanceof ServerPlayer killer)) return;

            Identifier killed = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (!killed.toString().equals(goal.target())) return;

            int n = GoalState.addKill(goal.target());
            if (n >= goal.amount()) {
                complete(killer.level().getServer(), killer);
            } else {
                killer.sendSystemMessage(Component.translatable("bmd.goal.progress",
                        n, goal.amount()).withStyle(ChatFormatting.GRAY), true);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++counter < CHECK_TICKS) return;
            counter = 0;
            if (!GoalState.isRunning()) return;

            Goal goal = GoalState.goal();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (matches(goal, player)) {
                    complete(server, player);
                    return;
                }
            }
        });
    }

    private static boolean matches(Goal goal, ServerPlayer player) {
        return switch (goal.type()) {
            case HAVE -> countItem(player, goal.target()) >= goal.amount();
            case REACH -> player.level().dimension().identifier().toString().equals(goal.target());
            case KILL -> false; // liczone eventem, nie skanowaniem
        };
    }

    private static int countItem(ServerPlayer player, String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return 0;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == Items.AIR) return 0;

        int n = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) n += stack.getCount();
        }
        return n;
    }

    private static void complete(MinecraftServer server, ServerPlayer by) {
        if (server == null || !GoalState.isRunning()) return;
        GoalState.finish(by.getUUID());

        Goal goal = GoalState.goal();
        Component time = Component.literal(formatTime(GoalState.elapsedMs()))
                .withStyle(ChatFormatting.GOLD);

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.empty());
            p.sendSystemMessage(Component.translatable("bmd.goal.done")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.translatable(goal.translationKey())
                    .withStyle(ChatFormatting.WHITE));
            p.sendSystemMessage(Component.translatable("bmd.goal.done_by",
                    Component.literal(by.getGameProfile().name()).withStyle(ChatFormatting.YELLOW), time));
            p.sendSystemMessage(Component.empty());
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    /** mm:ss albo h:mm:ss - bez milisekund, nikt ich nie czyta w biegu. */
    public static String formatTime(long ms) {
        long total = ms / 1000L;
        long h = total / 3600L;
        long m = (total % 3600L) / 60L;
        long s = total % 60L;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    private GoalTracker() {
    }
}
