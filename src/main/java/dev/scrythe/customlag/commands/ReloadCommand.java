package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.mixin.ConnectionAccessor;
import dev.scrythe.customlag.mixin.ServerCommonPacketListenerImplAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class ReloadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> reloadCommand = Commands.literal("reload")
                .executes(ReloadCommand::executeReloadCommand);
        return customLagCommand.then(reloadCommand);
    }

    private static void setPlayerLatencies(CommandContext<CommandSourceStack> context) {
        String[] playerNames = CustomLag.CONFIG.playerLag.keySet().toArray(new String[]{});
        PlayerList connectedPlayerList = context.getSource().getServer().getPlayerList();
        for (String playerName : playerNames) {
            ServerPlayer player = connectedPlayerList.getPlayerByName(playerName);
            if (player != null) {
                Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
                Channel channel = ((ConnectionAccessor) connection).getChannel();
                ChannelPipeline channelPipeline = channel.pipeline();

                int latency = CustomLag.CONFIG.playerLag.get(playerName);
                DelayingChannelDuplexHandler delayingChannelDuplexHandler = new DelayingChannelDuplexHandler(latency);
                channelPipeline.addBefore("packet_handler", "delay_handler", delayingChannelDuplexHandler);
            }
        }
    }

    private static int executeReloadCommand(CommandContext<CommandSourceStack> context) {
        LagCommand.removeAllPlayers(context);
        CustomLag.CONFIG = ConfigHandler.loadConfig(CustomLag.CONFIG_FILE);
        setPlayerLatencies(context);

        context.getSource().sendSuccess(() -> Component.literal("Reloaded config"), false);
        return Command.SINGLE_SUCCESS;
    }
}
