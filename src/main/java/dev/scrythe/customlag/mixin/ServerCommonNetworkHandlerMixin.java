package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    private int latency;

    @Inject(method = "onKeepAlive", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;latency:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void onKeepAlive(KeepAliveC2SPacket packet, CallbackInfo ci, @Local int i) {
        latency = i;
        updatePing();
    }

    @Unique
    private void updatePing() {
        ServerCommonNetworkHandler serverCommonNetworkHandler = (ServerCommonNetworkHandler) (Object) this;
        if (serverCommonNetworkHandler instanceof ServerPlayNetworkHandler) {
            ServerPlayerEntity player = ((ServerPlayNetworkHandler) serverCommonNetworkHandler).player;
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LATENCY, player);
            for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
                playerEntity.networkHandler.sendPacket(packet);
            }
        }
    }
}

