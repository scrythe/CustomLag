package dev.scrythe.customlag.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PlayerLagCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand, LiteralCommandNode<CommandSourceStack> lagNode) {
        LiteralArgumentBuilder<CommandSourceStack> playerLagCommand = Commands.literal("playerLag")
                .executes(PlayerLagCommand::executeDescription)
                .redirect(lagNode);
        return customLagCommand.then(playerLagCommand);
    }

    private static int executeDescription(CommandContext<CommandSourceStack> context) {
        int result = LagCommand.executeDescription(context);
        Component recommendLagCommand = Component.literal("(You can also use /lag instead)")
                .customLag$withClickCommand("lag");
        context.getSource().sendSuccess(() -> recommendLagCommand, false);
        return result;
    }
}
