package dev.scrythe.customlag.config;

import io.github.wasabithumb.jtoml.serial.TomlSerializable;

import java.util.HashMap;
import java.util.Map;

public class CustomLagConfig implements TomlSerializable {
    @ConfigOption
    @Comment("Whether to use only one Ping/Keepalive packet to determine the latency or the average of the latest 4.")
    public boolean useOnlyOnePingPacket = false;

    @ConfigOption
    @Comment("Custom Ping/Keepalive Packet Interval. Sending more frequent Ping Packets (but also reducing the time when a player gets kicked).")
    @Comment("Set to -1 to use default.")
    public long pingSendInterval = -1;

    @ConfigOption(client = true)
    @Comment("Show a numeral ping instead of the ping bar (only for client).")
    public boolean showNumeralPing = false;

    @ConfigOption(autoCommand = false)
    public Map<String, Integer> playerLag = new HashMap<>();
}
