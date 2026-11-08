# Faithful Library

**optional mods for beta 1.7.3**

Mods over [Old Faithful](https://github.com/studio-quire/old-faithful) and
[Still Faithful](https://github.com/studio-quire/still-faithful), in any combination, in single player and on servers.
A library pack comes with all of them on, and each has a switch; with every one off it plays exactly as its edition.
Open source, from
[Studio Quire](https://studioquire.org/).

They're mods, not a modified game: your launcher downloads Minecraft itself from Mojang, with your own account. For
Windows now.

## The mods

- **World Rules**: rules for each world, on the mod's page in the settings and on Create World's World Rules... page:
  Mobs Break Blocks, Blasts Destroy Items, Keep Inventory, Sleeping, Day Length (normal, double, triple), Weather (normal, always clear, always
  rain or snow), Natural Monsters, Monster Light (monsters appear in dim light as in beta, or not in any torch or lava
  light), Apple Drops (oak leaves, 1 time in 200) and Falling Hit Bonus (beta's, only when really falling, or none). A
  world nobody changed plays as beta. **Lock Rules** locks a world's rules so nothing in the game changes them; on a
  server, `/rule lock`. While they're locked, the Mods menu doesn't switch off a mod that has rules. Taking
  `rules-locked=true` out of the world's `faithful-world.properties`, with the game closed, unlocks them.
- **Buried Towers**: ruined towers stand on the land about every 24 chunks, and go a long way down, to about bedrock:
  floors somebody built and time got to, a different way down from each, in beta's own blocks. They take the place of
  beta's small dungeons in new chunks, and don't cut into land already made. A world chooses its towers when it's
  created, with **Towers** on Create World's World Rules... page, and keeps them: **Simple** (the default), crude stumps
  of cobble and moss like beta's dungeons, about 12 blocks across, or **Complex**, big towers of dressed stone, about
  23 across. (Better Dungeons, which this replaces, is gone: rooms it built stay in your worlds as ordinary blocks.)
- **Water Bottles**: glass bottles you fill at any water and drink from for nothing. Three glass in a V at
  the bench makes three. An ingredient for mods to come.
- **Hearty Food**: food heals half as much again, at half a heart a second instead of at once, and pale hearts show where
  you'll end up. Eating is one click as ever. More food adds to what's coming, a hit doesn't cancel it,
  and it helps even mid-fight. A golden apple still heals everything at once. Food past a full bar is wasted, as in beta.
- **Alchemy**: gild a rose, then brew it. Four gold ingots around a rose at the bench make a Golden Rose. A water bottle
  with one, two or three of them makes a Weak Health Potion, a Health Potion or a Strong Health Potion, in the
  inventory's grid too. Drinking heals 4, 6 or 8 hearts at once and leaves the bottle; at full health it isn't drunk. It
  needs Water Bottles, which comes on with it.
- **Blue Flowers**: a blue flower in patches across Taiga and Tundra, as dandelions grow in warmer land. New chunks only.
  It's placed and picked like the rose and dandelion, and makes two light blue dye. Bonemeal on grass in those biomes can
  grow one too.
- **Item Repair**: two worn tools, weapons or pieces of armour of the same kind in the crafting grid make one, with what
  was left of both, up to as good as new, as in later versions. Shears, fishing rods and flint and steel too. A
  world rule, Repair Bonus, adds a twentieth of a new one's durability on top, as later versions did; it's off to start
  with, so mending only puts together what you had, and there's nothing to gain by saving worn things for it.
- **Plain F3**: the F3 screen keeps its frame rate and memory and loses position, facing and entity counts, and mobs lose
  the numbers beta tags them with through walls, so you find your way without coordinates.
- **No Toasts**: no achievement or tutorial pop-ups. Achievements are still earned.

**Stagehand** is in the release as a jar of its own and in no pack: it runs scripts dropped into a folder in the game's
directory (teleports, items, time, weather, mobs, blocks), and writes back what happened, for setting up a play-test from
outside the game. Anything that can write to that folder can command the game, so install it only if you want that.
[docs/stagehand.md](docs/stagehand.md) says how.

**Faithful Core** is what they build on, and arrives only with them: none of the editions carries it, so a game with no
library mod on never says anything new on the network. **Faithful Settings** is the settings screen they share, which
Old and Still Faithful already have.

## Download and play

You need a Minecraft account that owns the game, and [Prism Launcher](https://prismlauncher.org/).

1. Download **OldFaithful-Library-&lt;version&gt;.zip** or **StillFaithful-Library-&lt;version&gt;.zip** from
   [the latest release](https://github.com/studio-quire/faithful-library/releases).
2. In Prism Launcher: **Add Instance > Import**, choose the zip (or paste the download link), **OK**.
3. Launch it. Prism downloads beta 1.7.3 and its sounds from Mojang, and, if you let it, a Java 21 runtime.
4. In the game: **Options > Old Faithful... > Mods...** (Still Faithful... in that pack). Each mod's page has an
   **Enabled** switch, which takes effect the next time you launch.

All the mods start on. Faithful Core comes on by itself when any mod is on, and goes off when none is. The jars are in
`.minecraft/mods-library`, and the switches are kept in `.minecraft/mods-library/selection.txt`.

There's no library pack for Pure Faithful, since Pure Faithful plus mods isn't Pure Faithful. Its mods can still run
them, as anyone's own setup can (below).

## Servers

`OldFaithful-Library-server-<version>.zip` and `StillFaithful-Library-server-<version>.zip` are that edition's server
package with the library's server-side mods on in `mods/`, as in the packs. So as they come they refuse a player without
Water Bottles, Alchemy, Blue Flowers and Buried Towers. Move out of `mods/` the jars you don't want, before the first start; `faithful-core` and
`oldfaithful-settings` stay while any library mod does. With them all out it's the plain edition's server. The README inside
says more.

- **World Rules** runs where the world runs, so a server can use it with any client, vanilla beta 1.7.3 too. A server with
  it is the same to a player who hasn't got it. `/rule <name> <value>` for ops and the console.
- **Hearty Food** runs where the world runs too. On a server with it food heals over time for every player, but only a
  player who has it too sees the pale hearts. With it on a server without it, food is beta's.
- **Item Repair** runs where the world runs too, so a server can use it with any client, vanilla beta 1.7.3 too. With it on a
  server without it, nothing is mended.
- **Water Bottles**, **Alchemy**, **Blue Flowers** and **Buried Towers** need every player to have them: the first three add blocks or items, and Buried Towers builds on the library's world generation, which needs it too. A server running one
  refuses anyone without it, vanilla clients included, with a message saying so. A player on a library pack with any mod
  on is offered **Match This Server's Mods**, which turns on what that server needs for their next launch. A client with
  them can join a server without them, where they can't be made and no towers are built.
- **Plain F3** and **No Toasts** are the player's alone, on any server.

With any library mod on, your game tells a server you join that you have the Faithful Library. A server with the library
sees that, and which of that server's own library mods you have too. It isn't told which other mods you have, Plain F3
and No Toasts included. A vanilla server ignores it.

Servers running other software (Project Poseidon and other Bukkit-family servers) can't load these mods. A client's mods
behave there as on a vanilla server.

## Turning a mod off or removing it

Worlds are kept in beta 1.7.3's own format, with the library's data beside it, so they open without the mods, in the
original game too. What you lose, plainly:

- **World Rules**: nothing. Its choices stay in the world's `faithful-world.properties` and come back with the mod. The
  hour can jump if the day's length was changed.
- **Buried Towers**: towers it's built stay, as ordinary blocks, and new chunks go back to beta's own dungeons. A tower half
  made stays half made. Rooms the old Better Dungeons built stay too.
- **Water Bottles**: its items don't survive. Bottles in that world become bowls for good. Alchemy goes off with it, as it needs it.
- **Alchemy**: its items don't survive. Golden roses in that world become roses and potions become bowls, for good.
- **Blue Flowers**: its flowers don't survive. Growing or carried, in that world they become roses, for good.
- **Hearty Food**: food goes back to beta's. Healing still to come waits in the player's save and goes on if the mod returns.
- **Item Repair**: nothing. What it mended stays mended, and Repair Bonus, if you turned it on, stays in the world's file.
- **Plain F3** and **No Toasts** leave nothing in a world.

While any library mod is on, Faithful Core is loaded and warns you before a world that's seen a removed content mod
opens: **Make a Backup and Play**, **Play**, **Cancel**. A server refuses to start, and says how to go on, each time. If you turn off
the last mod, the core goes too and **nothing warns you**, so copy the world's folder first. With the core gone, the
library's data in a chunk goes when that chunk is next saved. That data is what would bring a removed mod's blocks back, if
a later mod adds blocks: as long as the core stays, blocks survive a removed mod, showing as the vanilla block they stand
on, and return with it.

## For existing setups

The release has each mod's jar, `faithful-core` and `oldfaithful-settings` on their own. In a b1.7.3 Fabric (Babric)
instance, put the mods you want in `.minecraft/mods`, with those two. We test them on the three editions' mods; other
setups are untested. Faithful Core declares that it doesn't run with StationAPI, so that combination stops at launch with
a message.

## Writing a mod

[docs/library-authoring.md](docs/library-authoring.md) is the guide, and
[docs/library-harness.md](docs/library-harness.md) how mods are tested against each other. The Java API is 0.x and can
still change between releases. Names, save formats and what goes on the wire at login are permanent.

## Problems

Please [open an issue](https://github.com/studio-quire/faithful-library/issues) and attach
`.minecraft/logs/purefaithful/latest.log` from the instance folder. It never contains your access token.

## Building

This repository holds all of the library's source, and the editions' too. `./gw releaseLibrary` builds everything a release
contains into `local/release/faithful-library-<version>/`; [docs/building.md](docs/building.md) and
[docs/release.md](docs/release.md) say how. Inside, the project is called by its working name, `oldfaithful`.

## Credits and licence

Made by Claude Opus 5.5, an AI model by Anthropic, at Studio Quire. Idea, direction and play-testing: Shoalstone.

The library's code is under the [MIT licence](LICENSE). The packs build on Fabric Loader, Mixin, ASM, Babric's mappings,
LWJGL, SDL and OpenAL Soft, each under its own licence; the downloads carry their notices and licence texts, and the
source code their licences ask for.

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
