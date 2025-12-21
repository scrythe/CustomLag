package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.BiFunction;

public record ArgumentInfo<T>(ArgumentType<T> argumentType,
                              BiFunction<CommandContext<CommandSourceStack>, String, T> getArgumentValue) {
}

