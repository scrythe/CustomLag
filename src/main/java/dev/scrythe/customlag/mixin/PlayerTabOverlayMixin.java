package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.scrythe.customlag.CustomLagConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 20;

    @ModifyConstant(method = "render", constant = @Constant(intValue = 13))
    private int modifySlotWidthConstant(int original) {
        return original + PLAYER_SLOT_EXTRA_WIDTH;
    }

    @Unique
    private void renderPingText(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo) {
        Font font = Minecraft.getInstance().font;
        String pingString = playerInfo.getLatency() + "ms";
        int pingStringWidth = font.width(pingString);
        int textX = width + x - pingStringWidth;
        guiGraphics.drawString(Minecraft.getInstance().font, pingString, textX, y, -1, false);
    }

    @WrapMethod(method = "renderPingIcon")
    private void renderPingIconOrText(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo, Operation<Void> original) {
        if (CustomLagConfig.showNumeralPing) {
            renderPingText(guiGraphics, width, x, y, playerInfo);
        } else {
            original.call(guiGraphics, width, x, y, playerInfo);
        }
    }
}
