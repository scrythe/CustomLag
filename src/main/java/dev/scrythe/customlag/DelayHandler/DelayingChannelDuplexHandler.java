package dev.scrythe.customlag.DelayHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class DelayingChannelDuplexHandler extends ChannelDuplexHandler {
    private final PacketScheduler packetScheduler;
    private int delay;

    public DelayingChannelDuplexHandler(PacketScheduler packetScheduler, int latency) {
        this.packetScheduler = packetScheduler;
        delay = latency / 2;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        packetScheduler.delayPacket(() -> {
            ctx.fireChannelRead(msg);
        }, delay);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        packetScheduler.delayPacket(() -> {
            ctx.write(msg, promise);
            ctx.flush();
        }, delay);
    }

    public void changeLatency(int latency) {
        delay = latency / 2;
    }
}
