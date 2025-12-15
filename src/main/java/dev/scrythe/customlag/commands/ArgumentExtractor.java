package dev.scrythe.customlag.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface ArgumentExtractor<T> {
    T extract(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException;
}
