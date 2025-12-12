package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.scrythe.customlag.config.ConfigOption;
import dev.scrythe.customlag.config.CustomLagConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class ConfigCommand {
    private static final Map<Class<?>, ArgumentType<?>> argumentTypeMap = Map.of(Integer.class, IntegerArgumentType.integer(), boolean.class, BoolArgumentType.bool(), long.class, LongArgumentType.longArg(), ServerPlayer.class, EntityArgument.player());

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> customlagCommand = Commands.literal("customlag");
        for (Field field : CustomLagConfig.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);

            boolean isGameClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
            if (annotation.client() && !isGameClient) continue;

            LiteralArgumentBuilder<CommandSourceStack> fieldCommand = Commands.literal(field.getName());

            fieldCommand = fieldCommand.then(getCommand(field));
            if (annotation.set()) {
                fieldCommand = fieldCommand.then(setCommand(field));
            }
            fieldCommand = fieldCommand.then(resetCommand(field));
            if (annotation.add()) {
                fieldCommand = fieldCommand.then(setArrayCommand(field));
            }
            if (annotation.remove()) {
                fieldCommand = fieldCommand.then(removeCommand());
            }

            customlagCommand = customlagCommand.then(fieldCommand);
        }

        return customlagCommand;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getCommand(Field field) {
        return Commands.literal("get").executes(context -> executeGetCommand(context, field));
    }

    private static int executeGetCommand(CommandContext<CommandSourceStack> context, Field field) {
        try {
            Object fieldValue = field.get(CustomLagConfig.class);
            context.getSource()
                    .sendSuccess(() -> Component.literal("%s is set to %s".formatted(field.getName(), fieldValue)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setCommand(Field field) {
        ArgumentType<?> argumentType = argumentTypeMap.getOrDefault(field.getType(), StringArgumentType.string());
        RequiredArgumentBuilder<CommandSourceStack, ?> argumentCommand = Commands.argument(field.getName(), argumentType)
                .executes(context -> executeSetCommand(context, field));
        return Commands.literal("set").then(argumentCommand);
    }

    private static int executeSetCommand(CommandContext<CommandSourceStack> context, Field field) {
        Object fieldValue = context.getArgument(field.getName(), field.getType());
        try {
            field.set(CustomLagConfig.class, fieldValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set %s to %s".formatted(field.getName(), fieldValue)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> resetCommand(Field field) {
        Object defaultValue;
        try {
            defaultValue = field.get(CustomLagConfig.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Commands.literal("reset").executes(context -> executeResetCommand(context, field, defaultValue));
    }

    private static int executeResetCommand(CommandContext<CommandSourceStack> context, Field field, Object defaultValue) {
        try {
            field.set(CustomLagConfig.class, defaultValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Reset %s to %s (default)".formatted(field.getName(), defaultValue)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setArrayCommand(Field field) {
        ParameterizedType mapType = (ParameterizedType) field.getGenericType();
        Type[] mapTypeArguments = mapType.getActualTypeArguments();

        Class<?> keyType = (Class<?>) mapTypeArguments[0];
        Class<?> valueType = (Class<?>) mapTypeArguments[1];

        ArgumentType<?> keyArgumentType = argumentTypeMap.getOrDefault(keyType, StringArgumentType.string());
        ArgumentType<?> valueArgumentType = argumentTypeMap.getOrDefault(valueType, StringArgumentType.string());

        return Commands.literal("set")
                .then(Commands.argument(field.getName(), keyArgumentType)
                        .then(Commands.argument(field.getName(), valueArgumentType)
                                .executes(context -> executeSetArrayCommand(context, field, keyType, valueType))));
    }

    private static int executeSetArrayCommand(CommandContext<CommandSourceStack> context, Field field, Class<?> keyType, Class<?> valueType) {
        Object keyValue = context.getArgument(field.getName(), keyType);
        Object valueValue = context.getArgument(field.getName(), valueType);
        try {
            Map<Object, Object> map = (Map<Object, Object>) field.get(CustomLagConfig.class);
            map.put(keyValue, valueValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set %s with value %s to %s".formatted(keyValue, valueValue, field.getName())), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove");
    }
}
