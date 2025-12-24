package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CustomLagDescriptionCommand {
    public static int executeDescriptionCommand(CommandContext<CommandSourceStack> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Description of each subcommand:\n\n");
        sb.append("config: configure additional options, type in /customlag config fore more info\n");
        sb.append("playerlag: set the extra lag of a player, type in /customlag playerlag fore more info\n");
        sb.append("reload: reload the config from the customlag.toml file\n");
        sb.append("reset: reset everything to its default values");
        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return Command.SINGLE_SUCCESS;
    }
}
