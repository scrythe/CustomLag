package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import static net.minecraft.network.chat.Component.literal;
import net.minecraft.network.chat.MutableComponent;

public class CustomLagDescriptionCommand {
    public static int executeDescriptionCommand(CommandContext<CommandSourceStack> context) {
        MutableComponent descComponent = Component.empty();
        descComponent.append("Description of each subcommand:\n\n");

        descComponent.append("config: configure additional options, type in\n");
        descComponent.append(literal(" /customlag config\n").withStyle(ChatFormatting.ITALIC)
                .customLag$withClickCommand("customlag config"));
        descComponent.append(" fore more info\n");

        descComponent.append("playerlag: set the extra lag of a player, type in\n");
        descComponent.append(literal(" /customlag playerLag\n").withStyle(ChatFormatting.ITALIC)
                .customLag$withClickCommand("customlag playerLag"));
        descComponent.append(" fore more info\n");

        descComponent.append("reload: reload the config from the customlag.toml file\n");
        descComponent.append("reset: reset everything to its default values");
        context.getSource().sendSuccess(() -> descComponent, false);
        return Command.SINGLE_SUCCESS;
    }
}
