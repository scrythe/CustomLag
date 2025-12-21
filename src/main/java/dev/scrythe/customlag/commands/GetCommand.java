package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.config.CustomLagConfig;
import dev.scrythe.customlag.config.retentions.ConfigOption;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class GetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> resetCommand = Commands.literal("get")
                .executes(GetCommand::executeGetCommand);
        return customLagCommand.then(resetCommand);
    }

    private static int executeGetCommand(CommandContext<CommandSourceStack> context) {
        try {
            for (Field field : CustomLagConfig.class.getFields()) {
                Object fieldValue = field.get(CustomLag.CONFIG);
                context.getSource()
                        .sendSuccess(() -> Component.literal("%s=%s".formatted(field.getName(), fieldValue)), false);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }
}
