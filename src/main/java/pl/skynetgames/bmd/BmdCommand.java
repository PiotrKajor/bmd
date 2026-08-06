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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BmdCommand {

    private static final SuggestionProvider<CommandSourceStack> SENSES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(new String[]{"blind", "mute", "deaf", "none"}, builder);

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bmd")
                // bez argumentow: kazdy gracz moze sobie przypomniec, co mu wolno
                .executes(ctx -> info(ctx))
                .then(Commands.literal("info").executes(BmdCommand::info))

                .then(Commands.literal("losuj")
                        .requires(BmdCommand::isGameMaster)
                        .executes(ctx -> assignRandom(ctx.getSource(),
                                ctx.getSource().getServer().getPlayerList().getPlayers()))
                        .then(Commands.argument("gracze", EntityArgument.players())
                                .executes(ctx -> assignRandom(ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "gracze")))))

                .then(Commands.literal("ustaw")
                        .requires(BmdCommand::isGameMaster)
                        .then(Commands.argument("gracze", EntityArgument.players())
                                .then(Commands.argument("klasa", StringArgumentType.word())
                                        .suggests(SENSES)
                                        .executes(ctx -> assign(ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "gracze"),
                                                Sense.byName(StringArgumentType.getString(ctx, "klasa")))))))

                .then(Commands.literal("reset")
                        .requires(BmdCommand::isGameMaster)
                        .executes(BmdCommand::reset))

                .then(Commands.literal("hard")
                        .requires(BmdCommand::isGameMaster)
                        .then(Commands.argument("wlaczony", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"on", "off"}, b))
                                .executes(BmdCommand::hard)))

                .then(Commands.literal("lista")
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
            ctx.getSource().sendSuccess(() -> Component.literal("Ta komenda jest dla gracza w grze."), false);
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
        source.sendSuccess(() -> Component.literal("Rozdano klasy: " + n + " graczy.")
                .withStyle(ChatFormatting.GREEN), true);
        return n;
    }

    private static int assign(CommandSourceStack source, Collection<ServerPlayer> players, Sense sense) {
        for (ServerPlayer p : players) {
            apply(p, sense);
        }
        BmdSync.broadcast(source.getServer());
        final int n = players.size();
        source.sendSuccess(() -> Component.literal("Ustawiono " + sense.pl + " dla " + n + " gracz(y).")
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
            p.sendSystemMessage(Component.literal("Zmysly wrocily. Jestes zwyklym graczem.")
                    .withStyle(ChatFormatting.GREEN));
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Wyczyszczono wszystkie klasy.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int hard(CommandContext<CommandSourceStack> ctx) {
        boolean on = "on".equalsIgnoreCase(StringArgumentType.getString(ctx, "wlaczony"));
        BmdConfig.get().blindHardMode = on;
        BmdSync.broadcast(ctx.getSource().getServer());
        ctx.getSource().sendSuccess(() -> Component.literal("Tryb hard dla slepych: " + (on ? "WLACZONY" : "wylaczony"))
                .withStyle(on ? ChatFormatting.RED : ChatFormatting.GRAY), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Map<UUID, Sense> all = BmdState.all();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Nikt nie ma przydzielonej klasy."), false);
            return 0;
        }
        for (Map.Entry<UUID, Sense> e : all.entrySet()) {
            ServerPlayer p = source.getServer().getPlayerList().getPlayer(e.getKey());
            String name = p != null ? p.getGameProfile().name() : e.getKey().toString();
            Component line = Component.literal(" " + e.getValue().icon + " ").withStyle(e.getValue().color)
                    .append(Component.literal(name + " - " + e.getValue().pl).withStyle(ChatFormatting.WHITE));
            source.sendSuccess(() -> line, false);
        }
        return all.size();
    }

    private BmdCommand() {
    }
}
