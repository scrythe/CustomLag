package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.Channel;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Nullables;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow private int latency;

//    private void sendPing() {
//        this.waitingForKeepAlive = true;
//        this.lastKeepAliveTime = l;
//        this.keepAliveId = l;
//        this.sendPacket(new KeepAliveS2CPacket(this.keepAliveId));
//    }

    @Shadow public abstract int getLatency();

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Inject(
            method = "onKeepAlive",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;latency:I",
                    opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER
            )
    )
    public void onKeepAlive(KeepAliveC2SPacket packet, CallbackInfo ci, @Local int i) {
        latency = i;
        LOGGER.info("Latency: {}", i);
        updatePing();
    }

    @Unique
    private void updatePing() {
        ServerCommonNetworkHandler serverCommonNetworkHandler = (ServerCommonNetworkHandler) (Object) this;
        if (serverCommonNetworkHandler instanceof ServerPlayNetworkHandler){
            ServerPlayerEntity player = ((ServerPlayNetworkHandler) serverCommonNetworkHandler).player;
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LATENCY, player);
            sendPacket(packet);
        }
    }
}

