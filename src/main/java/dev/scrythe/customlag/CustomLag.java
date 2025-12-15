package dev.scrythe.customlag;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.scrythe.customlag.commands.ConfigCommand;
import dev.scrythe.customlag.commands.LagCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CustomLag implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> customLagCommand = Commands.literal("customlag")
                    .requires(source -> source.hasPermission(2));
            dispatcher.register(LagCommand.register(customLagCommand));
            dispatcher.register(ConfigCommand.register(customLagCommand));
        });
    }
}
