![customlag icon](./src/main/resources/assets/customlag/icon.png)

# CustomLag

![example usecase](https://i.imgur.com/IkmwseG.gif)

# About the Mod

This Mod simulates lag by delaying the processes of receiving and sending
packets. This is done by injecting a ContextHandler inside the ChannelPipeline
of a minecraft connection. This mod works only for servers and single player
worlds.

# Usage

To set or change the latency/ping for a player, just use the command /lag set
<playername> <latency>

you can use /lag remove <playername> to remove the ContextHandler that delays
packets from sending

Also, this mod changes how latency is displayed on the tab list. Instead of
showing an average of the last 4 keep alive packet latency, it only displays the
current latency one. The tablist also gets updated every time a new latency is
calculated
