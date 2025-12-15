package dev.scrythe.customlag.commands;

import com.mojang.brigadier.arguments.ArgumentType;

public record ArgumentInfo<P, R>(ArgumentType<P> argumentType,
                              ArgumentExtractor<R> getArgumentValue) {
}

