package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow @Final
    private static Logger LOGGER;
    @Shadow public ServerPlayerEntity player;
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ServerPlayNetworkHandler serverPlayNetworkHandler = (ServerPlayNetworkHandler) (Object) this;
    private boolean holdPacket = true;
    ThreadLocal<Integer> holdPacketThread = new ThreadLocal<>();
    
    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    public void onDisconnected(DisconnectionInfo info, CallbackInfo ci) {
        delayPacket(()->serverPlayNetworkHandler.onDisconnected(info),ci);
    }
    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        delayPacket(()->serverPlayNetworkHandler.onPlayerInteractEntity(packet),ci);
    }

    @Unique
    private void delayPacket(Runnable handler, CallbackInfo ci) {
        LOGGER.info("Thread: ");
        if (holdPacket) {
            service.schedule(()->{
                this.player.server.execute(()->{
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
        };
    }
}

