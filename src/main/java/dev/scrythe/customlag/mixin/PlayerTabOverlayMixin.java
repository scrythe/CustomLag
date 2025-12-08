package dev.scrythe.customlag.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 10;

    @ModifyConstant(method = "render", constant = @Constant(intValue = 13))
    private int modifySlotWidthConstant(int original) {
        return original + PLAYER_SLOT_EXTRA_WIDTH;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    private void renderPingText(PlayerTabOverlay instance, GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo) {
        Font font = Minecraft.getInstance().font;
        String pingString = playerInfo.getLatency() + "ms";
        int pingStringWidth = font.width(pingString);
        int textX = width + x - pingStringWidth;
        guiGraphics.drawString(Minecraft.getInstance().font, pingString, textX, y, 0xFFFFFF, false);
    }
}
