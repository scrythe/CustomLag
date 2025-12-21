package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

// send and receive packets delayed by half of latency and only whole numbers allowed therefore latency can only be even
public class EvenIntegerArgumentType implements ArgumentType<Integer> {
    @Override
    public Integer parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final int result = reader.readInt();
        if (result < 0) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, result, 0);
        }
        int isEvenNumber = result % 2;
        if (isEvenNumber != 0)
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Integer must be even.");
        return result;
    }

    public static int getInteger(final CommandContext<?> context, final String name) {
        return context.getArgument(name, int.class);
    }
}
