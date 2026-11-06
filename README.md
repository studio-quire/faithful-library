# Faithful Library

**optional mods for beta 1.7.3**

Mods over [Old Faithful](https://github.com/studio-quire/old-faithful) and
[Still Faithful](https://github.com/studio-quire/still-faithful), in any combination, in single player and on servers.
A library pack comes with all of them on, and each has a switch; with every one off it plays exactly as its edition.
Open source, from
[Studio Quire](https://studioquire.org/).

They're mods, not a modified game: your launcher downloads Minecraft itself from Mojang, with your own account. Nothing
of Mojang's is in this repository or in our downloads. For Windows now.

## The mods

- **World Rules**: rules for each world, on the mod's page in the settings and on Create World's World Rules... page:
  Mobs Break Blocks, Blasts Destroy Items, Keep Inventory, Sleeping, Day Length (normal, double, triple), Weather (normal, always clear, always
  rain or snow), Natural Monsters, Monster Light (monsters appear in dim light as in beta, or not in any torch or lava
  light), Apple Drops (oak leaves, 1 time in 200) and Falling Hit Bonus (beta's, only when really falling, or none). A
  world nobody changed plays as beta. **Lock Rules** locks a world's rules so nothing in the game changes them; on a
  server, `/rule lock`. Taking `rules-locked=true` out of the world's `faithful-world.properties`, with the game closed,
  unlocks them.
- **Better Dungeons**: beta's dungeons can grow. Corridors lead off the room to more rooms, buried and sealed, in beta's
  own blocks, with its monsters and chest list. Nothing outside a dungeon changes, and it only touches new chunks.
- **Water Bottles**: glass bottles you fill at any water and drink from for nothing. Three glass in a V at the bench
  makes three. An ingredient for mods to come.
- **Plain F3**: the F3 screen keeps its frame rate and memory and loses position, facing and entity counts, and mobs lose
  the numbers beta tags them with through walls, so you find your way without coordinates.
- **No Toasts**: no achievement or tutorial pop-ups. Achievements are still earned.

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
Water Bottles. Move out of `mods/` the jars you don't want, before the first start; `faithful-core` and
`oldfaithful-settings` stay while any library mod does. With them all out it's the plain edition's server. The README inside
says more.

- **World Rules** and **Better Dungeons** run where the world runs, so a server can use them with any client, vanilla
  beta 1.7.3 too. A server with them is the same to a player who hasn't got them. `/rule <name> <value>` for ops and the
  console.
- **Water Bottles** needs every player to have it, as anything that adds blocks or items does. A server running it
  refuses anyone without it, vanilla clients included, with a message saying so. A player on a library pack with any mod
  on is offered **Use This Server's Set**, which turns on what that server needs for their next launch. A client with
  Water Bottles can join a server without it, where the bottles can't be made.
- **Plain F3** and **No Toasts** are the player's alone, on any server.

With any library mod on, your login says you have Faithful Core. A server with the core can see that, and which of its
own mod channels you share. It isn't told which other mods you have, Plain F3 and No Toasts included. A vanilla server
ignores it.

Servers running other software (Project Poseidon and other Bukkit-family servers) can't load these mods. A client's mods
behave there as on a vanilla server.

## Turning a mod off or removing it

Worlds are kept in beta 1.7.3's own format, with the library's data beside it, so they open without the mods, in the
original game too. What you lose, plainly:

- **World Rules**: nothing. Its choices stay in the world's `faithful-world.properties` and come back with the mod. The
  hour can jump if the day's length was changed.
- **Better Dungeons**: rooms it built stay, as ordinary blocks. A dungeon it hadn't reached stays beta's single room.
- **Water Bottles**: its items don't survive. Bottles in that world become bowls for good.
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
