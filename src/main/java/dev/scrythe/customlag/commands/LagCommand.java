package dev.scrythe.customlag.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.mixin.ConnectionAccessor;
import dev.scrythe.customlag.mixin.ServerCommonPacketListenerImplAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LagCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("lag")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .suggests(new PlayerSuggestionProvider())
                                .then(Commands.argument("latency", IntegerArgumentType.integer(0))
                                        .executes(LagCommand::changeOrSetLatency))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .suggests(new PlayerSuggestionProvider())
                                .executes(LagCommand::removeLatency)));
    }

    private static int changeOrSetLatency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String playerName = player.getName().getString();
        int latency = IntegerArgumentType.getInteger(context, "latency");

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
        return 1;
    }

    private static int removeLatency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        String playerName = player.getName().getString();

        Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
        Channel channel = ((ConnectionAccessor) connection).getChannel();
        ChannelPipeline channelPipeline = channel.pipeline();

        if (channelPipeline.get("delay_handler") == null) {
            context.getSource()
                    .sendSuccess(() -> Component.literal("Lag for %s is not set. No need to remove".formatted(playerName)), false);
            return 0;
        }

        channelPipeline.remove("delay_handler");
        context.getSource().sendSuccess(() -> Component.literal("Remove lag for player %s".formatted(playerName)), false);
        return 1;
    }

    private static class PlayerSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            CommandSourceStack source = context.getSource();
            Collection<String> playerNames = source.getOnlinePlayerNames();
            for (String playerName : playerNames) {
                builder.suggest(playerName);
            }
            return builder.buildFuture();
        }
    }
}
