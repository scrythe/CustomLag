package dev.scrythe.customlag.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.LatencyColors;
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
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 35;

    @ModifyConstant(method = "render", constant = @Constant(intValue = 13), require = 0)
    private int modifySlotWidthConstant(int original) {
        if (CustomLag.CONFIG.showNumeralPing) {
            return original + PLAYER_SLOT_EXTRA_WIDTH;
        }
        return original;
    }

    @Unique
    private void renderPingText(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo) {
        Font font = Minecraft.getInstance().font;
        String pingString = playerInfo.getLatency() + "ms";
        int pingStringWidth = font.width(pingString);
        int textX = width + x - pingStringWidth;
        guiGraphics.drawString(Minecraft.getInstance().font, pingString, textX, y, LatencyColors.getColorOfLatency(playerInfo.getLatency())
                .getRGB());
    }

    @WrapMethod(method = "renderPingIcon")
    private void renderPingIconOrText(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo, Operation<Void> original) {
        if (CustomLag.CONFIG.showNumeralPing) {
            renderPingText(guiGraphics, width, x, y, playerInfo);
        } else {
            original.call(guiGraphics, width, x, y, playerInfo);
        }
    }
}
