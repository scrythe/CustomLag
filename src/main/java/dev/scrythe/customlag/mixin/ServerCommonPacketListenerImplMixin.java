package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    private int latency;

    @Shadow protected abstract boolean isSingleplayerOwner();
    @Shadow private boolean keepAlivePending;

    @Inject(method = "handleKeepAlive", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;latency:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void handleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci, @Local int i) {
        latency = i;
        updatePing();
    }

    @Redirect(method = "keepConnectionAlive", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;isSingleplayerOwner()Z"))
    protected boolean sendKeepPacketEvenToOwner(ServerCommonPacketListenerImpl instance) {
        return false;
    }

    @Redirect(method = "keepConnectionAlive", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;keepAlivePending:Z", opcode = Opcodes.GETFIELD))
    private boolean dontKickOwner(ServerCommonPacketListenerImpl instance) {
        return !this.isSingleplayerOwner() && this.keepAlivePending;
    }

    @Unique
    private void updatePing() {
        ServerCommonPacketListenerImpl serverCommonNetworkHandler = (ServerCommonPacketListenerImpl) (Object) this;
        if (serverCommonNetworkHandler instanceof ServerGamePacketListenerImpl) {
            ServerPlayer player = ((ServerGamePacketListenerImpl) serverCommonNetworkHandler).player;
            ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, player);
            for (ServerPlayer playerEntity : server.getPlayerList().getPlayers()) {
                playerEntity.connection.send(packet);
            }
        }
    }
}

