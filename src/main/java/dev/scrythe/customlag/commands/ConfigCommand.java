package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.scrythe.customlag.CustomLag;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.config.ConfigOption;
import dev.scrythe.customlag.config.CustomLagConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.Map;

public class ConfigCommand {
    private static final Map<Class<?>, ArgumentInfo<?>> argumentTypeMap = Map.of(Integer.class, new ArgumentInfo<>(IntegerArgumentType.integer(), IntegerArgumentType::getInteger), boolean.class, new ArgumentInfo<>(BoolArgumentType.bool(), BoolArgumentType::getBool), long.class, new ArgumentInfo<>(LongArgumentType.longArg(), LongArgumentType::getLong));
    public static final CustomLagConfig defaultConfig = new CustomLagConfig();

    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> configCommand = Commands.literal("config");
        for (Field field : CustomLagConfig.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);

            if (!annotation.autoCommand()) continue;
            boolean isGameClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
            if (annotation.client() && !isGameClient) continue;

            LiteralArgumentBuilder<CommandSourceStack> fieldCommand = Commands.literal(field.getName());

            fieldCommand = fieldCommand.then(getCommand(field));
            fieldCommand = fieldCommand.then(resetCommand(field));
            fieldCommand = fieldCommand.then(setCommand(field));

            configCommand = configCommand.then(fieldCommand);
        }

        return customLagCommand.then(configCommand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getCommand(Field field) {
        return Commands.literal("get").executes(context -> executeGetCommand(context, field));
    }

    private static int executeGetCommand(CommandContext<CommandSourceStack> context, Field field) {
        try {
            Object fieldValue = field.get(CustomLag.CONFIG);
            context.getSource()
                    .sendSuccess(() -> Component.literal("%s is set to %s".formatted(field.getName(), fieldValue)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setCommand(Field field) {
        ArgumentType<?> argumentType = argumentTypeMap.get(field.getType()).argumentType();
        RequiredArgumentBuilder<CommandSourceStack, ?> argumentCommand = Commands.argument(field.getName(), argumentType)
                .executes(context -> executeSetCommand(context, field));
        return Commands.literal("set").then(argumentCommand);
    }

    private static int executeSetCommand(CommandContext<CommandSourceStack> context, Field field) {
        try {
            Object fieldValue = argumentTypeMap.get(field.getType()).getArgumentValue().apply(context, field.getName());
            field.set(CustomLag.CONFIG, fieldValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set %s to %s".formatted(field.getName(), fieldValue)), false);
            ConfigHandler.writeConfig(CustomLag.CONFIG_FILE, CustomLag.CONFIG);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> resetCommand(Field field) {
        Object defaultValue;
        try {
            defaultValue = field.get(defaultConfig);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Commands.literal("reset").executes(context -> executeResetCommand(context, field, defaultValue));
    }

    private static int executeResetCommand(CommandContext<CommandSourceStack> context, Field field, Object defaultValue) {
        try {
            field.set(CustomLag.CONFIG, defaultValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Reset %s to %s (default)".formatted(field.getName(), defaultValue)), false);
            ConfigHandler.writeConfig(CustomLag.CONFIG_FILE, CustomLag.CONFIG);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
    }
}
