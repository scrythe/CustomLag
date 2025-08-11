package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.scrythe.customlag.Customlag;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerCommonNetworkHandler;
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
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandlerMixin {
    @Unique
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final private static Logger LOGGER;
    @Unique
    int delay = Customlag.latency / 2;

    @Inject(method = "onPlayerInput", at = @At("HEAD"))
    public void onPlayerInput(PlayerInputC2SPacket packet, CallbackInfo ci) {
        var stuff = Thread.currentThread().getName();
        LOGGER.info("hm");
    }

//    @WrapMethod(method = "onPlayerInput")
//    public void onPlayerInput(PlayerInputC2SPacket packet, Operation<Void> original) {
//        delayPacket(packet, original);
//    }
//
//    @WrapMethod(method = "onPlayerMove")
//    public void onPlayerMove(PlayerMoveC2SPacket packet, Operation<Void> original) {
//        delayPacket(packet, original);
//    }
//
//    @WrapMethod(method = "onPlayerInteractEntity")
//    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, Operation<Void> original) {
//        delayPacket(packet, original);
//    }
//
//    @Unique
//    private void delayPacket(Packet<ServerPlayPacketListener> packet, Operation<Void> original) {
//        channel.eventLoop().schedule(()->{
//            original.call(packet);
//        }, delay, TimeUnit.MILLISECONDS);
//    }
}
