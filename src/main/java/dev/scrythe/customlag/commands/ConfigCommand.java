package dev.scrythe.customlag.commands;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.scrythe.customlag.ConfigOption;
import dev.scrythe.customlag.CustomLagConfig2;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.Map;

public class ConfigCommand {
    private static final Map<Class<?>, ArgumentType<?>> argumentTypeMap = Map.of(int.class, IntegerArgumentType.integer(), Double.class, DoubleArgumentType.doubleArg(), Float.class, FloatArgumentType.floatArg(), String.class, StringArgumentType.string(), boolean.class, BoolArgumentType.bool(), long.class, LongArgumentType.longArg());

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> customlagCommand = Commands.literal("customlag");
        for (Field field : CustomLagConfig2.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);

            boolean isGameClient = false; // TEMP
            if (annotation.client() && !isGameClient) continue;

            LiteralArgumentBuilder<CommandSourceStack> fieldCommand = Commands.literal(field.getName());

            fieldCommand = fieldCommand.then(getCommand(field));
            if (annotation.set()) {
                fieldCommand = fieldCommand.then(setCommand(field));
            }
            fieldCommand = fieldCommand.then(resetCommand(field));
            if (annotation.add()) {
                fieldCommand = fieldCommand.then(addCommand(field));
            }
            if (annotation.remove()) {
                fieldCommand = fieldCommand.then(removeCommand());
            }

            customlagCommand = customlagCommand.then(fieldCommand);
        }

        return customlagCommand;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getCommand(Field field) {
        return Commands.literal("get").executes(context -> {
            try {
                Object fieldValue = field.get(CustomLagConfig2.class);
                context.getSource()
                        .sendSuccess(() -> Component.literal("%s is set to %s".formatted(field.getName(), fieldValue)), false);
            } catch (IllegalAccessException e) {
                context.getSource().sendFailure(Component.literal(e.toString()));
            }
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setCommand(Field field) {
        ArgumentType<?> argumentType = argumentTypeMap.getOrDefault(field.getType(), StringArgumentType.string());
        RequiredArgumentBuilder<CommandSourceStack, ?> argumentCommand = Commands.argument(field.getName(), argumentType)
                .executes(context -> {
                    Object fieldValue = context.getArgument(field.getName(), field.getType());
                    try {
                        field.set(CustomLagConfig2.class, fieldValue);
                        context.getSource()
                                .sendSuccess(() -> Component.literal("Set %s to %s".formatted(field.getName(), fieldValue)), false);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return 1;
                });
        return Commands.literal("set").then(argumentCommand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> resetCommand(Field field) {
        Object defaultValue;
        try {
            defaultValue = field.get(CustomLagConfig2.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Commands.literal("reset").executes(context -> {
            try {
                field.set(CustomLagConfig2.class, defaultValue);
                context.getSource()
                        .sendSuccess(() -> Component.literal("Reset %s to %s (default)".formatted(field.getName(), defaultValue)), false);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addCommand(Field field) {
        Object fieldType = field.getType().getComponentType();
        ArgumentType<?> argumentType = argumentTypeMap.getOrDefault(fieldType, StringArgumentType.string());
        RequiredArgumentBuilder<CommandSourceStack, ?> argumentCommand = Commands.argument(field.getName(), argumentType)
                .executes(context -> {
                    Object fieldValue = context.getArgument(field.getName(), field.getType());
                    try {
                        field.set(CustomLagConfig2.class, fieldValue);
                        context.getSource()
                                .sendSuccess(() -> Component.literal("Added %s to %s".formatted(fieldValue, field.getName())), false);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return 1;
                });
        return Commands.literal("add").then(argumentCommand);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove");
    }
}
