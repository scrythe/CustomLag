package dev.scrythe.customlag.commands.arguments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class VanillaArgumentType {
    protected static Component getErrorMessage(String errorMessage, String valueWhereError, String input, int cursor) {
        String prevCommandPartMessage = "..." + input.substring(cursor - 10, cursor);
        MutableComponent prevCommmandPartComponent = Component.literal(prevCommandPartMessage)
                .withStyle(ChatFormatting.GRAY);
        MutableComponent valueWhereErrorComponent = Component.literal(valueWhereError)
                .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
        MutableComponent errorPointerComponent = Component.literal("<--[HERE]")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);


        MutableComponent errorContextComponent = prevCommmandPartComponent.append(valueWhereErrorComponent)
                .append(errorPointerComponent)
                .withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/" + input)));
        return Component.literal(errorMessage.formatted(valueWhereError) + "\n").append(errorContextComponent);
    }
}
