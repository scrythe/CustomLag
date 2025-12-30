Welcome to the CustomLag wiki!

## Lag command

```mcfunction
/lag
```

Control how much lag is added to a player. <br> All send and receive packets are
delayed each by half of the latency value. Lag can be set and removed:

### Set command

```mcfunction
/lag set <player> <latency>
```

Set lag latency value for player(s). <br> (including offline players, will be
applied after joining automatically).

### Remove command

```mcfunction
/lag remove <player>
```

Remove lag for player(s).

## Customlag command

```mcfunction
/customlag
```

Make additional/advanced changes to config, resetting values etc. these include:

### Config command

```mcfunction
/customlag config
```

Configure additional options. <br> Go to
[Customlag config command](#customlag-config-command) fore more info.

### PlayerLag command

```mcfunction
/customlag playerLag
```

Set the extra lag of a player. <br> This is the same as the
[Lag command](#lag-command).

### Reload command

```mcfunction
/customlag reload
```

Reload the config from the customlag.toml file.

### Reset command

```mcfunction
/customlag reset
```

Reset everything to its default values.

## Customlag config command

```mcfunction
/customlag config
```

Customise certain options by changing the values of the field <br> All
subcommands of config.

### useOnlyOnePingPacket

Whether to use only one Ping/Keepalive packet to determine the latency or the
average of the latest 4.

Default value is false.

### pingSendInterval

Custom Ping/Keepalive Packet Interval. <br> Sending more frequent Ping Packets
(but also reducing the time when a player gets kicked). <br> Set to -1 to use
minecraft's default value.

Default value is -1.

### showNumeralPing

Show a numeral ping instead of the ping bar (this option works only for the
client).

Default value is false.
