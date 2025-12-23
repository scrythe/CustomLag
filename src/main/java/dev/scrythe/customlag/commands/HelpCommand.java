package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class HelpCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> helpCommand = Commands.literal("help")
                .executes(HelpCommand::executeHelpCommand);
        return customLagCommand.then(helpCommand);
    }

    private static int executeHelpCommand(CommandContext<CommandSourceStack> context) {
        return Command.SINGLE_SUCCESS;
    }
}
