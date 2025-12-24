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
import dev.scrythe.customlag.commands.arguments.ArgumentInfo;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.config.CustomLagConfig;
import dev.scrythe.customlag.config.retentions.ConfigOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigCommand {
    public static final CustomLagConfig defaultConfig = new CustomLagConfig();
    private static final Map<Class<?>, ArgumentInfo<?>> argumentTypeMap = Map.of(Integer.class, new ArgumentInfo<>(IntegerArgumentType.integer(), IntegerArgumentType::getInteger), boolean.class, new ArgumentInfo<>(BoolArgumentType.bool(), BoolArgumentType::getBool), long.class, new ArgumentInfo<>(LongArgumentType.longArg(), LongArgumentType::getLong));

    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> customLagCommand) {
        LiteralArgumentBuilder<CommandSourceStack> configCommand = Commands.literal("config");
        List<Field> fields = new ArrayList<>();
        for (Field field : CustomLagConfig.class.getFields()) {
            if (!field.isAnnotationPresent(ConfigOption.class)) continue;

            ConfigOption annotation = field.getAnnotation(ConfigOption.class);

            if (!annotation.autoCommand()) continue;
            boolean isGameClient = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
            if (annotation.client() && !isGameClient) continue;

            fields.add(field);

            LiteralArgumentBuilder<CommandSourceStack> fieldCommand = Commands.literal(field.getName())
                    .executes(context -> executeFieldDescription(context, field))
                    .then(setArgumentCommand(field));

            configCommand = configCommand.then(fieldCommand);
        }

        return customLagCommand.then(configCommand.executes((context -> executeConfigDescriptionCommand(context, fields))));
    }

    private static int executeConfigDescriptionCommand(CommandContext<CommandSourceStack> context, List<Field> fields) {
        MutableComponent descComponent = literal("Customise certain options by changing the values of the field\n");
        descComponent.append("All subcommands of config:\n\n");
        for (Field field : fields) {
            Object fieldValue;
            Object defaultValue;
            try {
                fieldValue = field.get(CustomLag.CONFIG);
                defaultValue = field.get(defaultConfig);
            } catch (IllegalAccessException e) {
                context.getSource().sendFailure(Component.literal(e.toString()));
                return -1;
            }
            descComponent.append(literal("%s <%s>\n".formatted(field.getName(), field.getType())).withStyle(ChatFormatting.GRAY)
                    .customLag$withClickCommand("/customlag config %s".formatted(field.getName())));
            if (defaultValue.equals(fieldValue)) {
                descComponent.append(translatable(" current value=%s (default))\n", literal(defaultValue.toString()).withStyle(ChatFormatting.UNDERLINE)));
            } else {
                descComponent.append(translatable(" current value=%s, default=%s\n", literal(fieldValue.toString()).withStyle(ChatFormatting.UNDERLINE), literal(defaultValue.toString()).withStyle(ChatFormatting.UNDERLINE)));
            }
        }
        descComponent.append("\nfor more information about each command, enter:\n");
        descComponent.append("/customlag config <subcommand>");
        context.getSource().sendSuccess(() -> descComponent, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeFieldDescription(CommandContext<CommandSourceStack> context, Field field) {
        MutableComponent descComponent = translatable("Description of %s:\n\n", literal(field.getName()).withStyle(ChatFormatting.UNDERLINE));
        descComponent.append(ConfigHandler.addComments(field));
        Object fieldValue;
        Object defaultValue;
        try {
            fieldValue = field.get(CustomLag.CONFIG);
            defaultValue = field.get(defaultConfig);
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
        Component valueInfoComponent = translatable("\nCurrent value of %s is %s", field.getName(), literal(fieldValue.toString()).withStyle(ChatFormatting.UNDERLINE));
        descComponent.append(valueInfoComponent);
        if (defaultValue.equals(fieldValue)) {
            descComponent.append(" (default)");
        } else {
            Component defaultValueComponent = translatable("\nDefault value is %s", literal(defaultValue.toString()).withStyle(ChatFormatting.UNDERLINE));
            descComponent.append(defaultValueComponent);
        }
        context.getSource().sendSuccess(() -> descComponent, false);
        return Command.SINGLE_SUCCESS;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> setArgumentCommand(Field field) {
        ArgumentType<?> argumentType = argumentTypeMap.get(field.getType()).argumentType();
        return Commands.argument("value", argumentType).executes(context -> executeSetCommand(context, field));
    }

    private static int executeSetCommand(CommandContext<CommandSourceStack> context, Field field) {
        try {
            Object fieldValue = argumentTypeMap.get(field.getType()).getArgumentValue().apply(context, "value");
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
}
