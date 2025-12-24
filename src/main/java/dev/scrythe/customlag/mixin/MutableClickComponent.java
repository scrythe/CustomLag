package dev.scrythe.customlag.mixin;

import dev.scrythe.customlag.IMutableClickComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(MutableComponent.class)
abstract public class MutableClickComponent implements IMutableClickComponent {
    @Override
    public MutableComponent customLag$withClickCommand(String input) {
        MutableComponent mutableComponent = (MutableComponent) (Object) this;
        return mutableComponent
                #if SELECTED_MINECRAFT_VERSION==MC_1_21_11
                .withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/" + input)));
                #else
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + input)));
                #endif
    }
}
