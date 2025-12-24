package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.commands.arguments.EvenIntegerArgumentType;
import dev.scrythe.customlag.commands.arguments.ExistigPlayerArgumentType;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.config.CustomLagConfig;
import dev.scrythe.customlag.mixin.ConnectionAccessor;
import dev.scrythe.customlag.mixin.EntitySelectorAccessor;
import dev.scrythe.customlag.mixin.ServerCommonPacketListenerImplAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class LagCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> playerLagCommand = Commands.literal("playerLag")
                .executes(LagCommand::executeDescription)
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.players())
                                .suggests(EntityArgument.player()::listSuggestions)
                                .then(Commands.argument("latency", EvenIntegerArgumentType.integer())
                                        .executes(LagCommand::executeSetPlayersCommand))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", ExistigPlayerArgumentType.players())
                                .suggests(ExistigPlayerArgumentType::listSuggestions)
                                .executes(LagCommand::executeRemovePlayersCommand)));
        return customLagCommand.then(playerLagCommand);
    }

    private static int executeDescription(CommandContext<CommandSourceStack> context) {
        Field field;
        try {
            field = CustomLagConfig.class.getField("playerLag");
        } catch (NoSuchFieldException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
        return ConfigCommand.executeFieldDescription(context, field);
    }

    private static int executeSetPlayersCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        EntitySelector entitySelector = context.getArgument("player", EntitySelector.class);
        List<ServerPlayer> players = entitySelector.findPlayers(context.getSource());
        int latency = EvenIntegerArgumentType.getInteger(context, "latency");
        if (players.isEmpty()) setPlayerName(context, entitySelector, latency);
        for (ServerPlayer player : players) {
            setPlayer(context, player, latency);
        }
        ConfigHandler.writeConfig(CustomLag.CONFIG_FILE, CustomLag.CONFIG);
        return Command.SINGLE_SUCCESS;
    }

    private static void setPlayerName(CommandContext<CommandSourceStack> context, EntitySelector entitySelector, int latency) {
        String playerName = ((EntitySelectorAccessor) entitySelector).getPlayerName();
        if (CustomLag.CONFIG.playerLag.containsKey(playerName)) {
            int prevLatency = CustomLag.CONFIG.playerLag.get(playerName);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Changed latency for (offline) player %s from %s to %s".formatted(playerName, prevLatency, latency)), false);
        } else {
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set latency for (offline) player %s to %s".formatted(playerName, latency)), false);
        }
        CustomLag.CONFIG.playerLag.put(playerName, latency);
    }

    private static void setPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player, int latency) {
        String playerName = player.getName().getString();

        Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
        Channel channel = ((ConnectionAccessor) connection).getChannel();
        ChannelPipeline channelPipeline = channel.pipeline();

        ChannelHandler channelHandler = channelPipeline.get("delay_handler");
        if (channelHandler instanceof DelayingChannelDuplexHandler delayingChannelDuplexHandler) {
            int prevLatency = delayingChannelDuplexHandler.getLatency();
            delayingChannelDuplexHandler.changeLatency(latency);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Changed latency for player %s from %s to %s".formatted(playerName, prevLatency, latency)), false);
        } else {
            DelayingChannelDuplexHandler delayingChannelDuplexHandler = new DelayingChannelDuplexHandler(latency);
            channelPipeline.addBefore("packet_handler", "delay_handler", delayingChannelDuplexHandler);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set latency for player %s to %s".formatted(playerName, latency)), false);
        }
        CustomLag.CONFIG.playerLag.put(playerName, latency);
    }

    private static int executeRemovePlayersCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String playerName = ExistigPlayerArgumentType.getPlayer(context, "player");
        if (playerName.equals("@a")) {
            String[] playerNames = removeAllPlayers(context);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Removed lag for: " + Arrays.toString(playerNames)), false);
        } else {
            PlayerList connectedPlayerList = context.getSource().getServer().getPlayerList();
            removePlayer(connectedPlayerList, playerName);
            context.getSource().sendSuccess(() -> Component.literal("Remove lag for player " + playerName), false);
        }
        ConfigHandler.writeConfig(CustomLag.CONFIG_FILE, CustomLag.CONFIG);
        return Command.SINGLE_SUCCESS;
    }

    public static String[] removeAllPlayers(CommandContext<CommandSourceStack> context) {
        String[] playerNames = CustomLag.CONFIG.playerLag.keySet().toArray(new String[]{});
        PlayerList connectedPlayerList = context.getSource().getServer().getPlayerList();
        for (String playerName : playerNames) {
            removePlayer(connectedPlayerList, playerName);
        }
        return playerNames;
    }


    private static void removePlayer(PlayerList connectedPlayerList, String playerName) {
        ServerPlayer player = connectedPlayerList.getPlayerByName(playerName);
        if (player != null) {
            Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
            Channel channel = ((ConnectionAccessor) connection).getChannel();
            ChannelPipeline channelPipeline = channel.pipeline();
            channelPipeline.remove("delay_handler");
        }
        CustomLag.CONFIG.playerLag.remove(playerName);
    }
}