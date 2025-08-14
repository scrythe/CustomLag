package dev.scrythe.customlag.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.mixin.ClientConnectionAccessor;
import dev.scrythe.customlag.mixin.ServerCommonNetworkHandlerAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class LagCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("lag")
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .suggests(new PlayerSuggestionProvider())
                                .then(CommandManager.argument("latency", IntegerArgumentType.integer(0))
                                        .executes(LagCommand::changeOrSetLatency))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .suggests(new PlayerSuggestionProvider())
                                .executes(LagCommand::removeLatency)));
    }

    private static int changeOrSetLatency(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        String playerName = player.getName().getString();
        int latency = IntegerArgumentType.getInteger(context, "latency");

        ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection();
        Channel channel = ((ClientConnectionAccessor) connection).getChannel();
        ChannelPipeline channelPipeline = channel.pipeline();

        ChannelHandler channelHandler = channelPipeline.get("delay_handler");
        if (channelHandler instanceof DelayingChannelDuplexHandler delayingChannelDuplexHandler) {
            int prevLatency = delayingChannelDuplexHandler.getLatency();
            delayingChannelDuplexHandler.changeLatency(latency);
            context.getSource()
                    .sendFeedback(() -> Text.literal("Changed latency for player %s from %s to %s".formatted(playerName, prevLatency, latency)), false);
        } else {
            DelayingChannelDuplexHandler delayingChannelDuplexHandler = new DelayingChannelDuplexHandler(latency);
            channelPipeline.addBefore("packet_handler", "delay_handler", delayingChannelDuplexHandler);
            context.getSource()
                    .sendFeedback(() -> Text.literal("Set latency for player %s to %s".formatted(playerName, latency)), false);
        }
        return 1;
    }

    private static int removeLatency(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        String playerName = player.getName().getString();

        ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection();
        Channel channel = ((ClientConnectionAccessor) connection).getChannel();
        ChannelPipeline channelPipeline = channel.pipeline();

        if (channelPipeline.get("delay_handler") == null) {
            context.getSource()
                    .sendFeedback(() -> Text.literal("Lag for %s is not set. No need to remove".formatted(playerName)), false);
            return 0;
        }

        channelPipeline.remove("delay_handler");
        context.getSource().sendFeedback(() -> Text.literal("Remove lag for player %s".formatted(playerName)), false);
        return 1;
    }

    private static class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            ServerCommandSource source = context.getSource();
            Collection<String> playerNames = source.getPlayerNames();
            for (String playerName : playerNames) {
                builder.suggest(playerName);
            }
            return builder.buildFuture();
        }
    }

}
