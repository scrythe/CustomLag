package dev.scrythe.customlag.DelayHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayingChannelOutboundHandler extends ChannelOutboundHandlerAdapter {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        service.schedule(()-> {
            try {
                ctx.write(msg, promise);
                ctx.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20, TimeUnit.MILLISECONDS);
    }
}
