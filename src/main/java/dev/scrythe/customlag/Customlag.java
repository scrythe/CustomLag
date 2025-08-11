package dev.scrythe.customlag;

import com.mojang.logging.LogUtils;
import dev.scrythe.customlag.mixin.ClientConnectionAccessor;
import dev.scrythe.customlag.DelayHandler.DelayingChannelInboundHandler;
import dev.scrythe.customlag.DelayHandler.DelayingChannelOutboundHandler;
import dev.scrythe.customlag.mixin.ServerCommonNetworkHandlerAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.ClientConnection;
import org.slf4j.Logger;

import java.util.Map;

public class Customlag implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static int latency = 1000;

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler,sender, server)->{
            ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
            Channel channel = ((ClientConnectionAccessor) connection).getChannel();
            ChannelPipeline channelPipeline = channel.pipeline();
//            if (channelPipeline.get("lag_handler") != null) {
//                LOGGER.info("lag");
//            }
            channelPipeline.addBefore("packet_handler", "delay_inbound_handler", new DelayingChannelInboundHandler());
            channelPipeline.addAfter("packet_handler", "delay_outbound_handler", new DelayingChannelOutboundHandler());
        });
//        ServerPlayConnectionEvents.DISCONNECT.register((handler, server)->{
//            ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
//            Channel channel = ((ClientConnectionAccessor) connection).getChannel();
//            ChannelPipeline channelPipeline = channel.pipeline();
//            channelPipeline.remove("lag_handler");
//        });
    }
}
