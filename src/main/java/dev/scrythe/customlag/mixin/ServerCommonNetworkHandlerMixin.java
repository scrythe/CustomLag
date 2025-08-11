package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.scrythe.customlag.Customlag;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    protected MinecraftServer server;

    @Unique
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Unique
    int delay = Customlag.latency / 2;

    @Shadow @Final
    protected ClientConnection connection;

    @Unique
    protected Channel channel;

    @Unique
    long startTime;
    @Unique
    long startOnKP;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData, CallbackInfo ci) {
        channel = ((ClientConnectionAccessor)connection).getChannel();
    }

    @WrapMethod(method = "sendPacket")
    public void sendPacket(Packet<?> packet, Operation<Void> original) {
        if (packet.getPacketType() == CommonPackets.KEEP_ALIVE_S2C) {
            LOGGER.info("keep alive");
            startTime = System.nanoTime();
        }
        service.schedule(()->{
            long delaySend = System.nanoTime()-startTime;
            if (packet.getPacketType() == CommonPackets.KEEP_ALIVE_S2C) {
                LOGGER.info("Delay Send: {}", delaySend/1_000_000L);
            }
            original.call(packet);
        }, delay, TimeUnit.MILLISECONDS);
    }
//
//    @WrapMethod(method = "onKeepAlive")
//    public void onKeepAlive(KeepAliveC2SPacket packet, Operation<Void> original) {
//        startOnKP = System.nanoTime();
//        channel.eventLoop().schedule(()->{
//            long current = System.nanoTime();
//            long latency = current - startTime;
//            long delayReceive = current - startOnKP;
//            LOGGER.info("Latency: {}", latency/1_000_000L);
//            LOGGER.info("Delay Receive: {}", delayReceive/1_000_000L);
//            original.call(packet);
//        }, delay, TimeUnit.MILLISECONDS);
//    }
}

