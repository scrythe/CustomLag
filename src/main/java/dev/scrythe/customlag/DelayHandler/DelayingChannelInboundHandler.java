package dev.scrythe.customlag.DelayHandler;

import io.netty.channel.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayingChannelInboundHandler extends ChannelInboundHandlerAdapter {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        service.schedule(() -> {
            try {
                ctx.fireChannelRead(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20, TimeUnit.MILLISECONDS);
    }
}