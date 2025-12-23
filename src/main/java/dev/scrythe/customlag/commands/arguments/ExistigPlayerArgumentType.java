package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.scrythe.customlag.CustomLag;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ExistigPlayerArgumentType {
    private static final DynamicCommandExceptionType PLAYER_NOT_CONFIGURED = new DynamicCommandExceptionType(playerName ->Component.literal("Player %s has not ben set".formatted(playerName)));

    public static StringArgumentType players() {
        return StringArgumentType.string();
    }

    public static String getPlayer(final CommandContext<?> context, final String name) throws CommandSyntaxException {
        String result = context.getArgument(name, String.class);
        if (result.equals("@a")) return result;
        Collection<String> playerNames = CustomLag.CONFIG.playerLag.keySet();
        if (!playerNames.contains(result)) {
            String input = context.getInput();
            StringReader reader = new StringReader(input);
            reader.setCursor(input.length());
            //throw PLAYER_NOT_CONFIGURED.createWithContext(reader, result);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, result, 0);
        }
        return result;
    }

    public static <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Set<String> playerNames = CustomLag.CONFIG.playerLag.keySet();
        if (!playerNames.isEmpty()) {
            playerNames = new HashSet<>(playerNames);
            playerNames.add("@a");
        }
        return SharedSuggestionProvider.suggest(playerNames, builder);
    }
}
