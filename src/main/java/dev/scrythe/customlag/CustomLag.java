package dev.scrythe.customlag;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.scrythe.customlag.commands.ConfigCommand;
import dev.scrythe.customlag.commands.EvenIntegerArgumentType;
import dev.scrythe.customlag.commands.ExistigPlayerArgumentType;
import dev.scrythe.customlag.commands.LagCommand;
import dev.scrythe.customlag.config.ConfigHandler;
import dev.scrythe.customlag.config.CustomLagConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class CustomLag implements ModInitializer {
    public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("customlag.toml");
    public static CustomLagConfig CONFIG;

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(ResourceLocation.fromNamespaceAndPath("fabric-docs", "even_integer"), EvenIntegerArgumentType.class, SingletonArgumentInfo.contextFree(EvenIntegerArgumentType::new));
        ArgumentTypeRegistry.registerArgumentType(ResourceLocation.fromNamespaceAndPath("fabric-docs", "existing_player"), ExistigPlayerArgumentType.class, SingletonArgumentInfo.contextFree(ExistigPlayerArgumentType::new));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> customLagCommand = Commands.literal("customlag")
                    .requires(source -> source.hasPermission(2));
            dispatcher.register(LagCommand.register(customLagCommand));
            dispatcher.register(ConfigCommand.register(customLagCommand));
        });

        CONFIG = ConfigHandler.loadConfig(CONFIG_FILE);
        ConfigHandler.writeConfig(CONFIG_FILE, CONFIG);
    }
}
