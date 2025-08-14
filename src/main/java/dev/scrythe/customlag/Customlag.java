package dev.scrythe.customlag;

import com.mojang.logging.LogUtils;
import dev.scrythe.customlag.DelayHandler.DelayingChannelDuplexHandler;
import dev.scrythe.customlag.DelayHandler.PacketScheduler;
import dev.scrythe.customlag.mixin.ClientConnectionAccessor;
import dev.scrythe.customlag.mixin.ServerCommonNetworkHandlerAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.ClientConnection;
import org.slf4j.Logger;

public class Customlag implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PacketScheduler packetScheduler = new PacketScheduler();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//            LOGGER.info("joined server");
            ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
            Channel channel = ((ClientConnectionAccessor) connection).getChannel();
            ChannelPipeline channelPipeline = channel.pipeline();
            DelayingChannelDuplexHandler delayingChannelDuplexHandler = new DelayingChannelDuplexHandler(packetScheduler, 1000);
            channelPipeline.addBefore("packet_handler", "delay_handler", delayingChannelDuplexHandler);
//            for (String name : channelPipeline.names()) {
//                System.out.println(name);
//            }
//            delayingChannelDuplexHandler.changeLatency(1000);
        });

    }
}
