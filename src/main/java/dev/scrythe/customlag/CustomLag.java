package dev.scrythe.customlag;

import dev.scrythe.customlag.commands.ConfigCommand;
import dev.scrythe.customlag.commands.LagCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.nio.file.Paths;

public class CustomLag implements ModInitializer {
public static final CustomLagConfig CONFIG = CustomLagConfig.createToml(Paths.get("config"), "", "customlag", CustomLagConfig.class);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LagCommand.register());
            dispatcher.register(ConfigCommand.register());
        });
    }
}
