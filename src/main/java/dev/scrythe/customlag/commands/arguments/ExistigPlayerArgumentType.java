package dev.scrythe.customlag.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.scrythe.customlag.CustomLag;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ExistigPlayerArgumentType implements ArgumentType<String> {
    public static String getPlayer(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String result = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        if (result.equals("@a")) return result;
        Collection<String> playerNames = CustomLag.CONFIG.playerLag.keySet();
        if (!playerNames.contains(result)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Integer must be even.");
        }
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Set<String> playerNames = CustomLag.CONFIG.playerLag.keySet();
        if (!playerNames.isEmpty()) {
            playerNames = new HashSet<>(playerNames);
            playerNames.add("@a");
        }
        return SharedSuggestionProvider.suggest(playerNames, builder);
    }
}
