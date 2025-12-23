package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;

// send and receive packets delayed by half of latency and only whole numbers allowed therefore latency can only be even
public class EvenIntegerArgumentType extends VanillaArgumentType {
    private static final Dynamic3CommandExceptionType UNEVEN_INTEGER = new Dynamic3CommandExceptionType((valueWhereError, input, cursor) -> getErrorMessage("Integer %s must be even.", (String) valueWhereError, (String) input, (int) cursor));

    public static IntegerArgumentType integer() {
        return IntegerArgumentType.integer(0);
    }

    public static int getInteger(final CommandContext<?> context, final String name) throws CommandSyntaxException {
        int result = context.getArgument(name, int.class);
        int isEvenNumber = result % 2;
        if (isEvenNumber != 0) {

            // CommandSyntaxException uneven_integer_exception = UNEVEN_INTEGER.createWithContext(reader, result);
//            throw new CommandSyntaxException(
//                    uneven_integer_exception.getType(),
//                    Component.literal(uneven_integer_exception.getMessage()) // getMessage() has the arrow!
            // );
            String input = context.getInput();
            String resultString = String.valueOf(result);
            int cursor = input.lastIndexOf(resultString);
            throw UNEVEN_INTEGER.create(resultString, input, cursor);
            }
        return result;
    }
}
