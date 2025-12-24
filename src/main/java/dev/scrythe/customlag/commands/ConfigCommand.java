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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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

        return customLagCommand.then(configCommand
                .executes((context -> executeConfigDescriptionCommand(context, fields))));
    }

    private static int executeConfigDescriptionCommand(CommandContext<CommandSourceStack> context, List<Field> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customise certain options by changing the values of the field\n");
        sb.append("All subcommands of config:\n\n");
        for (Field field : fields) {
            sb.append("%s <%s>\n".formatted(field.getName(), field.getType()));
        }
        sb.append("\nfor more information about each command, enter:\n");
        sb.append("/customlag config <subcommand>");
        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeFieldDescription(CommandContext<CommandSourceStack> context, Field field) {
        StringBuilder sb = new StringBuilder();
        sb.append("Description of %s:\n\n".formatted(field.getName()));
        ConfigHandler.addComments(field, sb, false);
        Object fieldValue;
        Object defaultValue;
        try {
            fieldValue = field.get(CustomLag.CONFIG);
            defaultValue = field.get(defaultConfig);
        } catch (IllegalAccessException e) {
            context.getSource().sendFailure(Component.literal(e.toString()));
            return -1;
        }
        sb.append("\nCurrent value of %s is %s\n".formatted(field.getName(), fieldValue));
        sb.append("Default value is ").append(defaultValue);
        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
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
