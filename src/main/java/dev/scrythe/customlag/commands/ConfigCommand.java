package dev.scrythe.customlag.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.scrythe.customlag.config.ConfigOption;
import dev.scrythe.customlag.config.ConfigOptionMap;
import dev.scrythe.customlag.config.CustomLagConfig;
import dev.scrythe.customlag.config.PostSetter;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.*;
import java.util.Map;

public class ConfigCommand {
    private static final Map<Class<?>, ArgumentInfo<?, ?>> argumentTypeMap = Map.of(Integer.class, new ArgumentInfo<>(IntegerArgumentType.integer(), IntegerArgumentType::getInteger), boolean.class, new ArgumentInfo<>(BoolArgumentType.bool(), BoolArgumentType::getBool), long.class, new ArgumentInfo<>(LongArgumentType.longArg(), LongArgumentType::getLong), ServerPlayer.class, new ArgumentInfo<>(EntityArgument.player(), EntityArgument::getPlayer));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> customlagCommand = Commands.literal("customlag");
        LiteralArgumentBuilder<CommandSourceStack> customlagConfigCommand = Commands.literal("config");
        for (Field field : CustomLagConfig.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);

            boolean isGameClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
            if (annotation.client() && !isGameClient) continue;

            LiteralArgumentBuilder<CommandSourceStack> fieldCommand = Commands.literal(field.getName());

            fieldCommand = fieldCommand.then(getCommand(field));
            fieldCommand = fieldCommand.then(resetCommand(field));

            boolean isMap = field.isAnnotationPresent(ConfigOptionMap.class);
            if (isMap) {
                ConfigOptionMap mapAnnotation = field.getAnnotation(ConfigOptionMap.class);
                fieldCommand = fieldCommand.then(setMapCommand(field, mapAnnotation));
                fieldCommand = fieldCommand.then(removeCommand());
            } else {
                fieldCommand = fieldCommand.then(setCommand(field));
            }

            if (annotation.isParentCommandConfig()) {
                customlagConfigCommand = customlagConfigCommand.then(fieldCommand);
            } else {
                customlagCommand = customlagCommand.then(fieldCommand);
            }
        }

        return customlagCommand.then(customlagConfigCommand);
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
            Object fieldValue = argumentTypeMap.get(field.getType()).getArgumentValue().extract(context, field.getName());
            field.set(CustomLagConfig.class, fieldValue);
            context.getSource()
                    .sendSuccess(() -> Component.literal("Set %s to %s".formatted(field.getName(), fieldValue)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalAccessException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
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
            return -1;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setMapCommand(Field field, ConfigOptionMap mapAnnotation) {
        ParameterizedType mapType = (ParameterizedType) field.getGenericType();
        Type[] mapTypeArguments = mapType.getActualTypeArguments();

        Class<?> keyType = (Class<?>) mapTypeArguments[0];
        Class<?> valueType = (Class<?>) mapTypeArguments[1];

        ArgumentType<?> keyArgumentType = argumentTypeMap.get(keyType).argumentType();
        ArgumentType<?> valueArgumentType = argumentTypeMap.get(valueType).argumentType();

        boolean executePostSetter = field.isAnnotationPresent(PostSetter.class);

        return Commands.literal("set")
                .then(Commands.argument(mapAnnotation.keyName(), keyArgumentType)
                        .then(Commands.argument(mapAnnotation.valueName(), valueArgumentType)
                                .executes(context -> executeSetMapCommand(context, field, mapAnnotation, keyType, valueType, executePostSetter))));
    }

    private static int executeSetMapCommand(CommandContext<CommandSourceStack> context, Field field, ConfigOptionMap mapAnnotation, Class<?> keyType, Class<?> valueType, boolean executePostSetter) {
        try {
            Object keyValue = argumentTypeMap.get(keyType)
                    .getArgumentValue()
                    .extract(context, mapAnnotation.keyName());
            Object valueValue = argumentTypeMap.get(valueType)
                    .getArgumentValue()
                    .extract(context, mapAnnotation.valueName());

            Map<Object, Object> map = (Map<Object, Object>) field.get(CustomLagConfig.class);
            map.put(keyValue, valueValue);

            if (executePostSetter) {
                String postSetterName = "postSet" + Character.toUpperCase(field.getName().charAt(0)) + field.getName()
                        .substring(1);
                Method postSetter = CustomLagConfig.class.getMethod(postSetterName, CommandContext.class, keyType, valueType);
                return (int) postSetter.invoke(null, context, keyValue, valueValue);
            } else {
                context.getSource()
                        .sendSuccess(() -> Component.literal("Set %s with value %s to %s".formatted(keyValue, valueValue, field.getName())), false);
                return Command.SINGLE_SUCCESS;
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                 CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove");
    }
}
