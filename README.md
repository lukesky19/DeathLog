# DeathLog
## Description
* DeathLog logs player death locations, reasons, inventories, and experience. Inventories and experience can be restored.

## Features
* Logs player death locations, reasons, inventories, and experience.
* Commands to restore inventory contents and experience lost on death.

## Dependencies
* [SkyLib](https://github.com/lukesky19/SkyLib)

## Commands
* /deathlog - The base command
* /deathlog restore <player_name> <death_id> <inventory | exp> - Restore a player's inventory or exp from when they died.
* /deathlog give <give_to_player> <target_player_name> <death_id> <inventory | exp> - Give the target player's inventory or exp from when they died to the give-to player.
* /deathlog info <player_name> <death_id> - View the data logged for the player and death id.

## Permissions
* `deathlog.command.deathlog` - Base Command Permission
* `deathlog.command.deathlog.restore` - Permission to restore a player's inventory when they died.
* `deathlog.command.deathlog.give` - Permission to give a player another a player's inventory when they died.
* `deathlog.command.deathlog.exp` - Permission to restore a player's exp when they died.
* `deathlog.command.deathlog.info` - Permission to view a player's deaths.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, and 1.21.8.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot and Paper?

A: Only Paper is supported. There are no plans to support any other server software (i.e., Spigot, Folia).

## Issues, Bugs, or Suggestions
* Please create a new [GitHub Issue](https://github.com/lukesky19/deathlog/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to deathlog and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
