package dev.scrythe.customlag.DelayHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class DelayingChannelDuplexHandler extends ChannelDuplexHandler {
    private int delay;

    public DelayingChannelDuplexHandler(int latency) {
        delay = latency / 2;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PacketScheduler.delayPacket(() -> {
            ctx.fireChannelRead(msg);
        }, delay);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PacketScheduler.delayPacket(() -> {
            ctx.write(msg, promise);
            ctx.flush();
        }, delay);
    }

    public int getLatency() {
        return delay * 2;
    }

    public void changeLatency(int latency) {
        delay = latency / 2;
    }
}
