package dev.scrythe.customlag;

import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;

public class CustomLagConfig2 {
    @Comment("Whether to use only one Ping/Keepalive packet to determine the latency or the average of the latest 4.")
    @ConfigOption()
    public static boolean useOnlyOnePingPacket = false;


    @Comment("Custom Ping/Keepalive Packet Interval. Sending more frequent Ping Packets (but also reducing the time when a player gets kicked).")
    @Comment("Set to -1 to use default.")
    @ConfigOption()
    public static long pingSendInterval = -1;


    @Comment("Show a numeral ping instead of the ping bar (only for client).")
    @ConfigOption(client = true)
    public static boolean showNumeralPing = false;
}
