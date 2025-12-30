<img src="./src/main/resources/assets/customlag/icon.png" alt="customlag icon" width="100"/>

# CustomLag

<!-- for modrinth
<video autoplay muted loop playsinline controls preload="auto">
  <source src="https://raw.githubusercontent.com/scrythe/CustomLag/refs/heads/main/assets/example-usecase.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video>
-->

<!-- for local:
<video autoplay muted loop playsinline controls preload="auto">
  <source src="./assets/example-usecase.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video>
-->

<!-- for github -->

https://github.com/user-attachments/assets/df095434-80d9-4b04-8a91-cd1e9d70a50e

# About the Mod

This Mod simulates lag by delaying the processes of receiving and sending
packets. This is done by injecting a ContextHandler inside the ChannelPipeline
of a minecraft connection. This mod works only for servers and single player
worlds.

# Usage

To set or change the latency/ping for a player, just use the lag command:

```mcfunction
/lag set <playername> <latency>
```

Setting lag will be saved to a toml file, so even after restart, the settings
will remain. You can also set the lag of offline players (who have not currently
joined yet.)

In order to remove the lag of a player, you can use:

```mcfunction
/lag remove <playername>
```

(this removes the ContextHandler that delays packets from sending)

Some additional commands are:

There are also some addional quality of live -ish commands, like increase the
keep alive / ping packets interval, to find more, just enter

```mcfunction
/customlag
```
