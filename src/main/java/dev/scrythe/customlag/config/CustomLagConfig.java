package dev.scrythe.customlag.config;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class CustomLagConfig {
    @Comment("Whether to use only one Ping/Keepalive packet to determine the latency or the average of the latest 4.")
    @ConfigOption(isParentCommandConfig = true)
    public static boolean useOnlyOnePingPacket = false;


    @Comment("Custom Ping/Keepalive Packet Interval. Sending more frequent Ping Packets (but also reducing the time when a player gets kicked).")
    @Comment("Set to -1 to use default.")
    @ConfigOption(isParentCommandConfig = true)
    public static long pingSendInterval = -1;


    @Comment("Show a numeral ping instead of the ping bar (only for client).")
    @ConfigOption(isParentCommandConfig = true, client = true)
    public static boolean showNumeralPing = false;

    @ConfigOption(isParentCommandConfig = false)
    @ConfigOptionMap(keyName = "player", valueName = "latency")
    @PostSetter
    public static Map<String, Integer> playerLag = new HashMap<>();

    public static int postSetPlayerLag(CommandContext<CommandSourceStack> context, ServerPlayer player, Integer latency) {
        context.getSource()
                .sendSuccess(() -> Component.literal("Set lag of player %s to %s".formatted(player.getName().getString(), latency)), false);
        return -1;
    }
}
