package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class VanillaArgumentException {
    public static Dynamic3CommandExceptionType getCommandExceptionType(String errorMessage) {
        return new Dynamic3CommandExceptionType((valueWhereError, input, cursor) -> getErrorMessage(errorMessage, (String) valueWhereError, (String) input, (int) cursor));
    }

    private static Component getErrorMessage(String errorMessage, String valueWhereError, String input, int cursor) {
        String prevCommandPartMessage = "..." + input.substring(cursor - 10, cursor);
        MutableComponent prevCommmandPartComponent = Component.literal(prevCommandPartMessage)
                .withStyle(ChatFormatting.GRAY);
        MutableComponent valueWhereErrorComponent = Component.literal(valueWhereError)
                .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
        MutableComponent errorPointerComponent = Component.literal("<--[HERE]")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);


        MutableComponent errorContextComponent = prevCommmandPartComponent.append(valueWhereErrorComponent)
                .append(errorPointerComponent).customLag$withClickCommand(input);
        return Component.literal(errorMessage.formatted(valueWhereError) + "\n").append(errorContextComponent);
    }
}
