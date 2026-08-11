package pl.skynetgames.bmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import pl.skynetgames.bmd.goal.Goal;
import pl.skynetgames.bmd.goal.GoalState;
import pl.skynetgames.bmd.goal.GoalTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BmdCommand {

    private static final java.util.Random RANDOM = new java.util.Random();

    private static final SuggestionProvider<CommandSourceStack> SENSES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(new String[]{"blind", "mute", "deaf", "none"}, builder);

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bmd")
                // bez argumentow: kazdy gracz moze sobie przypomniec, co mu wolno
                .executes(ctx -> info(ctx))
                .then(Commands.literal("info").executes(BmdCommand::info))

                .then(Commands.literal("random")
                        .requires(BmdCommand::isGameMaster)
                        .executes(ctx -> assignRandom(ctx.getSource(),
                                ctx.getSource().getServer().getPlayerList().getPlayers()))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> assignRandom(ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "players")))))

                .then(Commands.literal("set")
                        .requires(BmdCommand::isGameMaster)
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("class", StringArgumentType.word())
                                        .suggests(SENSES)
                                        .executes(ctx -> assign(ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "players"),
                                                Sense.byName(StringArgumentType.getString(ctx, "class")))))))

                .then(Commands.literal("reset")
                        .requires(BmdCommand::isGameMaster)
                        .executes(BmdCommand::reset))

                .then(Commands.literal("mode")
                        .requires(BmdCommand::isGameMaster)
                        .then(Commands.argument("level", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                        new String[]{"easy", "normal", "hard"}, b))
                                .executes(BmdCommand::mode)))

                .then(Commands.literal("goal")
                        .executes(BmdCommand::goalInfo)
                        .then(Commands.literal("info").executes(BmdCommand::goalInfo))
                        .then(Commands.literal("random")
                                .requires(BmdCommand::isGameMaster)
                                .then(Commands.argument("difficulty", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                                new String[]{"easy", "normal", "hard"}, b))
                                        .executes(BmdCommand::goalRandom)))
                        .then(Commands.literal("set")
                                .requires(BmdCommand::isGameMaster)
                                .then(Commands.argument("goal", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                                Goal.all().stream().map(Goal::id).toList(), b))
                                        .executes(BmdCommand::goalSet)))
                        .then(Commands.literal("list")
                                .then(Commands.argument("difficulty", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                                new String[]{"easy", "normal", "hard"}, b))
                                        .executes(BmdCommand::goalList)))
                        .then(Commands.literal("clear")
                                .requires(BmdCommand::isGameMaster)
                                .executes(BmdCommand::goalClear)))

                .then(Commands.literal("list")
                        .requires(BmdCommand::isGameMaster)
                        .executes(BmdCommand::list)));
    }

    private static boolean isGameMaster(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            ctx.getSource().sendSuccess(() -> Component.translatable("bmd.cmd.player_only"), false);
            return 0;
        }
        BmdSync.briefing(player);
        return 1;
    }

    private static int assignRandom(CommandSourceStack source, Collection<ServerPlayer> players) {
        // Rownomierny rozdzial: tasujemy pule klas zamiast losowac kazdemu osobno,
        // inaczej przy czterech graczach czesto wypada trzy razy to samo.
        List<Sense> pool = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            pool.add(Sense.PLAYABLE[i % Sense.PLAYABLE.length]);
        }
        java.util.Collections.shuffle(pool);

        int i = 0;
        for (ServerPlayer p : players) {
            apply(p, pool.get(i++));
        }
        BmdSync.broadcast(source.getServer());
        final int n = players.size();
        source.sendSuccess(() -> Component.translatable("bmd.cmd.assigned", n)
                .withStyle(ChatFormatting.GREEN), true);
        return n;
    }

    private static int assign(CommandSourceStack source, Collection<ServerPlayer> players, Sense sense) {
        for (ServerPlayer p : players) {
            apply(p, sense);
        }
        BmdSync.broadcast(source.getServer());
        final int n = players.size();
        source.sendSuccess(() -> Component.translatable("bmd.cmd.set", sense.displayName(), n)
                .withStyle(ChatFormatting.GREEN), true);
        return n;
    }

    private static void apply(ServerPlayer player, Sense sense) {
        BmdState.set(player.getUUID(), sense);
        player.sendSystemMessage(Component.empty());
        BmdSync.briefing(player);
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        BmdState.clearAll();
        BmdSync.broadcast(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.translatable("bmd.cmd.reset").withStyle(ChatFormatting.GREEN));
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("bmd.cmd.cleared")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int mode(CommandContext<CommandSourceStack> ctx) {
        BlindMode m = BlindMode.byName(StringArgumentType.getString(ctx, "level"));
        BmdConfig.get().blindMode = m.name();
        BmdConfig.save();
        MinecraftServer server = ctx.getSource().getServer();
        BmdSync.broadcast(server);
        // Opis klasy zalezy od trybu, wiec slepi dostaja swiezy briefing.
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (BmdState.get(p) == Sense.BLIND) BmdSync.briefing(p);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("bmd.cmd.blind_mode", m.displayName())
                .withStyle(m == BlindMode.HARD ? ChatFormatting.RED : ChatFormatting.GREEN), true);
        return 1;
    }

    // ───────────────────────────── cele ─────────────────────────────

    private static int goalInfo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Goal goal = GoalState.goal();
        if (goal == null) {
            source.sendSuccess(() -> Component.translatable("bmd.goal.none").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("bmd.goal.current")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("  ").append(
                Component.translatable(goal.translationKey()).withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("  ").append(
                Component.translatable("bmd.goal.difficulty." + goal.difficulty().name().toLowerCase())
                        .withStyle(ChatFormatting.AQUA)), false);
        source.sendSuccess(() -> Component.literal("  ").append(
                Component.translatable("bmd.goal.time",
                        GoalTracker.formatTime(GoalState.elapsedMs())).withStyle(ChatFormatting.YELLOW)), false);
        if (GoalState.isFinished()) {
            source.sendSuccess(() -> Component.translatable("bmd.goal.already_done")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        return 1;
    }

    private static int goalRandom(CommandContext<CommandSourceStack> ctx) {
        Goal.Difficulty d = Goal.Difficulty.byName(StringArgumentType.getString(ctx, "difficulty"));
        List<Goal> pool = Goal.byDifficulty(d);
        Goal picked = pool.get(RANDOM.nextInt(pool.size()));
        return startGoal(ctx.getSource(), picked);
    }

    private static int goalSet(CommandContext<CommandSourceStack> ctx) {
        Goal goal = Goal.byId(StringArgumentType.getString(ctx, "goal"));
        if (goal == null) {
            ctx.getSource().sendSuccess(() -> Component.translatable("bmd.goal.unknown")
                    .withStyle(ChatFormatting.RED), false);
            return 0;
        }
        return startGoal(ctx.getSource(), goal);
    }

    private static int startGoal(CommandSourceStack source, Goal goal) {
        GoalState.start(goal);
        MinecraftServer server = source.getServer();
        BmdSync.broadcastGoal(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.empty());
            p.sendSystemMessage(Component.translatable("bmd.goal.started")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            p.sendSystemMessage(Component.literal("  ").append(
                    Component.translatable(goal.translationKey()).withStyle(ChatFormatting.WHITE)));
            p.sendSystemMessage(Component.literal("  ").append(
                    Component.translatable("bmd.goal.difficulty." + goal.difficulty().name().toLowerCase())
                            .withStyle(ChatFormatting.AQUA)));
            p.sendSystemMessage(Component.empty());
        }
        return 1;
    }

    private static int goalList(CommandContext<CommandSourceStack> ctx) {
        Goal.Difficulty d = Goal.Difficulty.byName(StringArgumentType.getString(ctx, "difficulty"));
        CommandSourceStack source = ctx.getSource();
        for (Goal g : Goal.byDifficulty(d)) {
            Component line = Component.literal(" • " + g.id() + " - ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable(g.translationKey()).withStyle(ChatFormatting.WHITE));
            source.sendSuccess(() -> line, false);
        }
        return Goal.byDifficulty(d).size();
    }

    private static int goalClear(CommandContext<CommandSourceStack> ctx) {
        GoalState.clear();
        BmdSync.broadcastGoal(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.translatable("bmd.goal.cleared")
                .withStyle(ChatFormatting.GRAY), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Map<UUID, Sense> all = BmdState.all();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("bmd.cmd.nobody"), false);
            return 0;
        }
        for (Map.Entry<UUID, Sense> e : all.entrySet()) {
            ServerPlayer p = source.getServer().getPlayerList().getPlayer(e.getKey());
            String name = p != null ? p.getGameProfile().name() : e.getKey().toString();
            Component line = Component.literal(" ").append(e.getValue().emoji())
                    .append(Component.literal(" " + name + " - ").withStyle(e.getValue().color))
                    .append(e.getValue().displayName().copy().withStyle(e.getValue().color));
            source.sendSuccess(() -> line, false);
        }
        return all.size();
    }

    private BmdCommand() {
    }
}
