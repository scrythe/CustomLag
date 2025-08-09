package dev.scrythe.customlag.mixin;

import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final protected MinecraftServer server;
    ServerCommonNetworkHandler serverCommonNetworkHandler = (ServerCommonNetworkHandler) (Object) this;
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private boolean holdPacket = true;
    ThreadLocal<Integer> holdPacketThread = new ThreadLocal<>();

    long startTime;
    long startOnKP;

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
//        if (packet.getPacketType() == CommonPackets.KEEP_ALIVE_S2C) {
//            LOGGER.info("keep alive");
//        }
        startTime = System.nanoTime();
        service.schedule(()->{
//            long delaySend = System.nanoTime()-startTime;
//            if (packet.getPacketType() == CommonPackets.KEEP_ALIVE_S2C) {
//                LOGGER.info("Delay Send: {}", delaySend/1_000_000L);
//            }
            serverCommonNetworkHandler.send(packet, null);
        }, 20, TimeUnit.MILLISECONDS);
        ci.cancel();
    }

    @Inject(method = "onKeepAlive", at = @At("HEAD"), cancellable = true)
    public void onKeepAlive(KeepAliveC2SPacket packet, CallbackInfo ci) {
        delayPacket(()-> serverCommonNetworkHandler.onKeepAlive(packet), true, ci);
    }

    @Unique
    private void delayPacket(Runnable handler, boolean isKeepAlive, CallbackInfo ci) {
//        LOGGER.info("Test: {}", holdPacketThread.get());
        if (holdPacket) {
            if (isKeepAlive) {
                startOnKP = System.nanoTime();
            }
            service.schedule(()->{
                this.server.execute(()->{
                    holdPacket=false;
                    holdPacketThread.set(5);
                    handler.run();
                    holdPacket=true;
                });
//                holdPacket=false;
//                holdPacketThread.set(5);
//                handler.run();
//                holdPacket=true;
            }, 20, TimeUnit.MILLISECONDS);
            ci.cancel();
            return;
        };
        if (isKeepAlive) {
            long current = System.nanoTime();
            long latency = current - startTime;
            long delayReceive = current - startOnKP;
//            LOGGER.info("Latency: {}", latency/1_000_000L);
//            LOGGER.info("Delay Receive: {}", delayReceive/1_000_000L);
//            LOGGER.info("Test: {}", holdPacketThread.get());
        }
    }
}

