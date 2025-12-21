package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.config.retentions.ConfigOption;
import dev.scrythe.customlag.config.CustomLagConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ResetCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> resetCommand = Commands.literal("reset")
                .executes(ResetCommand::executeResetCommand);
        return customLagCommand.then(resetCommand);
    }

    public static void resetAllFields() throws IllegalAccessException {
        for (Field field : CustomLagConfig.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;
            ConfigOption annotation = field.getAnnotation(ConfigOption.class);
            if (!annotation.autoCommand()) continue;

            Object defaultValue = field.get(ConfigCommand.defaultConfig);
            field.set(CustomLag.CONFIG, defaultValue);
        }
    }

    private static int executeResetCommand(CommandContext<CommandSourceStack> context) {
        LagCommand.removeAllPlayers(context);
        try {
            resetAllFields();
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
        context.getSource().sendSuccess(() -> Component.literal("Reset total config"), false);
        ConfigHandler.writeConfig(CustomLag.CONFIG_FILE, CustomLag.CONFIG);
        return Command.SINGLE_SUCCESS;
    }
}
