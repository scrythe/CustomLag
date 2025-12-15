package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LagCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> playerLagCommand = Commands.literal("playerLag");
        return customLagCommand.then(playerLagCommand.then(Commands.literal("get")
                        .then(Commands.argument("player", new ExistigPlayerArgumentType()).executes(LagCommand::getPlayers))
                        .executes(LagCommand::getAllPlayers))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.players())
                                .suggests(EntityArgument.player()::listSuggestions)
                                .then(Commands.argument("latency", new EvenIntegerArgumentType())
                                        .executes(LagCommand::setPlayers))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", new ExistigPlayerArgumentType())
                                .executes(LagCommand::removePlayers)))
                .then(Commands.literal("reset").executes(LagCommand::removeAllPlayers)));
    }

    private static int getPlayers(CommandContext<CommandSourceStack> context) {
        String playerName = ExistigPlayerArgumentType.getPlayer(context, "player");
        if (playerName.equals("@a")) return getAllPlayers(context);
        int latency = CustomLagConfig.playerLag.get(playerName);
        context.getSource()
                .sendSuccess(() -> Component.literal("Player latency of %s is set to %s".formatted(playerName, latency)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getAllPlayers(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendSuccess(() -> Component.literal("Player=Latency Map: " + CustomLagConfig.playerLag.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        EntitySelector entitySelector = context.getArgument("player", EntitySelector.class);
        List<ServerPlayer> players = entitySelector.findPlayers(context.getSource());
        int latency = EvenIntegerArgumentType.getInteger(context, "latency");
        if (players.isEmpty()) setPlayerName(context, entitySelector, latency);
        for (ServerPlayer player : players) {
            setPlayer(context, player, latency);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void setPlayerName(CommandContext<CommandSourceStack> context, EntitySelector entitySelector, int latency) {
        String playerName = ((EntitySelectorAccessor) entitySelector).getPlayerName();
        if (CustomLagConfig.playerLag.containsKey(playerName)) {
            int prevLatency = CustomLagConfig.playerLag.get(playerName);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Changed latency for (offline) player %s from %s to %s".formatted(playerName, prevLatency, latency)), false);
        } else {
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set latency for (offline) player %s to %s".formatted(playerName, latency)), false);
        }
        CustomLagConfig.playerLag.put(playerName, latency);
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
        CustomLagConfig.playerLag.put(playerName, latency);
    }

    private static int removePlayers(CommandContext<CommandSourceStack> context) {
        String playerName = ExistigPlayerArgumentType.getPlayer(context, "player");
        if (playerName.equals("@a")) {
            return removeAllPlayers(context);
        } else {
            PlayerList connectedPlayerList = context.getSource().getServer().getPlayerList();
            removePlayer(connectedPlayerList, playerName);
            context.getSource().sendSuccess(() -> Component.literal("Remove lag for player " + playerName), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeAllPlayers(CommandContext<CommandSourceStack> context) {
        Set<String> playerNames = CustomLagConfig.playerLag.keySet();
        PlayerList connectedPlayerList = context.getSource().getServer().getPlayerList();
        for (String playerName : playerNames) {
            removePlayer(connectedPlayerList, playerName);
        }
        context.getSource()
                .sendSuccess(() -> Component.literal("Removed lag for: " + playerNames), false);
        return Command.SINGLE_SUCCESS;
    }

    private static void removePlayer(PlayerList connectedPlayerList, String playerName) {
        ServerPlayer player = connectedPlayerList.getPlayerByName(playerName);
        if (player != null) {
            Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
            Channel channel = ((ConnectionAccessor) connection).getChannel();
            ChannelPipeline channelPipeline = channel.pipeline();
            channelPipeline.remove("delay_handler");
        }
        CustomLagConfig.playerLag.remove(playerName);
    }
}