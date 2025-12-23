package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.scrythe.customlag.CustomLag;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ExistigPlayerArgumentType {
    private static final Dynamic3CommandExceptionType PLAYER_NOT_CONFIGURED = VanillaArgumentException.getCommandExceptionType("Player %s has not ben set.");

    public static StringArgumentType players() {
        return StringArgumentType.string();
    }

    public static String getPlayer(final CommandContext<?> context, final String name) throws CommandSyntaxException {
        String result = context.getArgument(name, String.class);
        if (result.equals("@a")) return result;
        Collection<String> playerNames = CustomLag.CONFIG.playerLag.keySet();
        if (!playerNames.contains(result)) {
            String input = context.getInput();
            int cursor = input.lastIndexOf(result);
            throw PLAYER_NOT_CONFIGURED.create(result, input, cursor);
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
