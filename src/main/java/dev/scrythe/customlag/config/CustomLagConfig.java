package dev.scrythe.customlag.config;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.mixin.ConnectionAccessor;
import dev.scrythe.customlag.mixin.ServerCommonPacketListenerImplAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class CustomLagConfig {
    @Comment("Whether to use only one Ping/Keepalive packet to determine the latency or the average of the latest 4.")
    @ConfigOption
    public static boolean useOnlyOnePingPacket = false;


    @Comment("Custom Ping/Keepalive Packet Interval. Sending more frequent Ping Packets (but also reducing the time when a player gets kicked).")
    @Comment("Set to -1 to use default.")
    @ConfigOption
    public static long pingSendInterval = -1;


    @Comment("Show a numeral ping instead of the ping bar (only for client).")
    @ConfigOption(client = true)
    public static boolean showNumeralPing = false;

    @ConfigOption(add = true)
    public static Map<ServerPlayer, Integer> playerLagMap = new HashMap<>();

    public static void setPlayerMap(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        String playerName = player.getName().getString();
        int latency = IntegerArgumentType.getInteger(context, "latency");

        Connection connection = ((ServerCommonPacketListenerImplAccessor) player.connection).getConnection();
        Channel channel = ((ConnectionAccessor) connection).getChannel();
        ChannelPipeline channelPipeline = channel.pipeline();
        ChannelHandler channelHandler = channelPipeline.get("delay_handler");

        if (channelHandler instanceof DelayingChannelDuplexHandler delayingChannelDuplexHandler) {
            delayingChannelDuplexHandler.changeLatency(latency);
        } else {
            DelayingChannelDuplexHandler delayingChannelDuplexHandler = new DelayingChannelDuplexHandler(latency);
            channelPipeline.addBefore("packet_handler", "delay_handler", delayingChannelDuplexHandler);
        }

        playerLagMap.put(player, latency);
    }
}
