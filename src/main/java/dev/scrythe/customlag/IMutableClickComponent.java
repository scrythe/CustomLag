package dev.scrythe.customlag;

import net.minecraft.network.chat.MutableComponent;

public interface IMutableClickComponent {
    default MutableComponent customLag$withClickCommand(String input) {
        return (MutableComponent) this;
    }
}
