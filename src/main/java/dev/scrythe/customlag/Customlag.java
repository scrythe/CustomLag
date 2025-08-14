package dev.scrythe.customlag;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import dev.scrythe.customlag.commands.LagCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

public class Customlag implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static int executeLagCommand(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Called command /lag"), false);
        return 1;
    }

    @Override
    public void onInitialize() {
//        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//            for (String name : channelPipeline.names()) {
//                System.out.println(name);
//            }
//        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LagCommand.register());
        });
    }
}
