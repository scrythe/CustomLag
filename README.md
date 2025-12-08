<img src="./src/main/resources/assets/customlag/icon.png" alt="customlag icon" width="100"/>

# CustomLag

<!-- for modrinth
<video autoplay muted loop playsinline controls preload>
  // only for local: <source src="./src/main/resources/assets/customlag/example-usecase.mp4" type="video/mp4">
  <source src="https://github.com/scrythe/CustomLag/raw/refs/heads/main/src/main/resources/assets/customlag/example-usecase.mp4" type="video/mp4">
  Your browser does not support the video tag.
</video> -->


<!-- for github -->
<video src="https://github.com/user-attachments/assets/af988608-fc0a-472d-b2a4-939ccbc7fb63" type="video/mp4">
  Your browser does not support the video tag.
</video>

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
