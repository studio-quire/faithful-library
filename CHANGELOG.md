# Changelog

The Faithful Library's releases, newest first. One version number for Faithful Core, the library's mods, the library
packs and server packages. Each names the edition releases its packs are built on, whose mods and LWJGL are inside as
those releases have them, and Faithful Settings is Old Faithful's. Entries say what a player or server owner notices.

## 0.2.2 (2026-11-09)

Library packs for Old Faithful 0.2.3 and Still Faithful 0.4.1, on Pure Faithful 0.1.5. The packs now carry those
releases; the library's own mods are unchanged. From Still Faithful 0.4.1 (in its pack): its Skeleton Arrows switch,
off unless you turned it on, is gone. Its changelog has the detail.

## 0.2.1 (2026-11-09)

Library packs for Old Faithful 0.2.3 and Still Faithful 0.4.0, on Pure Faithful 0.1.5. The packs carry those
releases: from Pure Faithful, servers that make new land faster; from Old Faithful, mobs that stop being solid as they
die; and from Still Faithful (in its pack), chests that pair as in later versions with one lid, shift-click that fills
dispensers and furnaces and puts armour on, and skeletons that can shoot from their eyes (off unless turned on).
Their changelogs have the detail.

- **Buried Towers** works out its towers in less time as new land is made, so a new tower holds the game up for less
  of a moment, the first of each game most of all. Every tower comes out exactly as before: towers in your worlds
  carry on as they were.

## 0.2.0 (2026-11-08)

Library packs for Old Faithful 0.2.2 and Still Faithful 0.3.0, on Pure Faithful 0.1.4. Six new mods, and the packs
carry those releases: squid that swim and a fix for a phantom extra item after closing a chest on a server from Old Faithful, fences
shaped as in later versions and short chat bars from Still Faithful (in its pack), and a fix for a rare crash making a
new server world from Pure Faithful. Their changelogs have the detail.

- **Buried Towers**, a new mod: ruined towers stand on the land about every 24 chunks, and go a long way down: floors
  somebody built and time got to, and ways down worth finding. They replace beta's small dungeons in new chunks. A
  world chooses its towers when it's created, with the rule Towers on Create World's World Rules... page: Simple (the
  default), crude stumps of cobble and moss like beta's dungeons, or Complex, big towers of dressed stone. A server
  running it needs every player to have it.
- **Better Dungeons is gone.** Buried Towers replaces it. Rooms it built stay in your worlds as ordinary blocks, and worlds
  carry on as they were. Its id is retired; a pack's list that still names it just drops the line.
- **Hearty Food**, a new mod: food heals half as much again, at half a heart a second instead of at once, and pale hearts
  show where you'll end up. Eating is one click as ever. More food adds to what's coming, a hit doesn't
  cancel it, and it helps even mid-fight. A golden apple still heals everything at once. Cake isn't eaten once a full
  bar is on its way. It runs where the world runs: any client can join a server with it, and only a client with it too
  sees the pale hearts.
- **Stagehand**, a new mod as a jar of its own, in no pack or server package: scripts dropped into a folder in the game's
  directory set up a play-test from outside it (teleports, items, time, weather, mobs, blocks).
- **Alchemy**, a new mod: gild a rose, then brew it. Four gold ingots around a rose at the bench make a Golden Rose. A
  water bottle with one, two or three of them makes a Weak Health Potion, a Health Potion or a Strong Health Potion, in
  the inventory's grid too. Drinking heals 4, 6 or 8 hearts at once and leaves the bottle; at full health it isn't
  drunk. A server running it needs every player to have it. It needs Water Bottles.
- **Blue Flowers**, a new mod: a blue flower in patches across Taiga and Tundra, as dandelions grow in warmer land. New
  chunks only. It's placed and picked like the rose and dandelion, and makes two light blue dye. Bonemeal on grass in
  those biomes can grow one too. A server running it needs every player to have it.
- **Item Repair**, a new mod: two worn tools, weapons or pieces of armour of the same kind in the crafting grid make
  one, with what was left of both, up to as good as new, as in later versions. A world rule, Repair Bonus, off to
  start with, adds a twentieth of a new one's durability on top. It runs where the world runs: any client can join a
  server with it and mend there.
- **World Rules**: Monster Light's second choice is called Dark Only (it was None): any torch or lava light stops
  monsters appearing.
- Locked rules: while a world's rules are locked (or the server's you're on), the Mods menu doesn't switch off a mod
  that has rules (World Rules, Buried Towers, Item Repair); from the title screen it does. The game no longer says how
  to unlock them; the README still does.
- When a server refuses you over its mods, the screen names the mods as you know them, says which the server needs and
  which to turn off to join, and where the log is. Its button is now **Match This Server's Mods**.
- Faithful Core: fixed a rare crash when a server makes a new world, about once in 900 (stopped at "Preparing level"),
  on any server running the library, with or without Pure Faithful's mods.

## 0.1.4 (2026-11-06)

Library packs for Old Faithful 0.2.1 and Still Faithful 0.2.2, on Pure Faithful 0.1.3. The packs now carry those releases;
the library's own mods are unchanged.

From Old Faithful 0.2.1, each on its own switch, under Fixes > Players and Vehicles:

- With a block low overhead (a roof 2 above the floor, say), a step could sink you about a third of a block into the
  floor or the block ahead, and you were shoved back out. A step now comes down only as far as it went up. Mobs sank
  the same way and no longer do (Steps Under Roofs).
- Rejoining a world right against a wall, a rounding slip could put you a hair inside it. You then walked into the wall,
  suffocating if it reached your eyes, until you were pushed out. The sides of your box now stop at the wall (Rejoining
  by Walls).

The Rejoining Worlds switch (`world.rejoinTerrain`) is gone: in our tests it made no difference a player would notice,
and a config file that still names it loads as before.
[Old Faithful's changelog](https://github.com/studio-quire/old-faithful/blob/main/CHANGELOG.md) has the correction that
goes with this.

We can't show that these two fixes cover every time you've been moved into a block and pushed out. If it happens again,
please tell us in an issue where you were and whether anything was overhead.

## 0.1.3 (2026-11-06)

Library packs for Old Faithful 0.2.0 and Still Faithful 0.2.1, on Pure Faithful 0.1.3.

- **World Rules**: a new rule, Blasts Destroy Items. On, as in beta, explosions of any kind destroy items lying on the
  ground. Off, they only throw them.

## 0.1.2 (2026-11-06)

Library packs for Old Faithful 0.2.0 and Still Faithful 0.2.1, on Pure Faithful 0.1.3.

- The server packages now start with every server-side mod on in `mods/`, to match the packs. With Water Bottles on, a new
  server refuses players without it, vanilla clients and the plain editions' packs included. Take it out of `mods/` before
  the first start to keep such a server open to them.

## 0.1.1 (2026-11-06)

Library packs for Old Faithful 0.2.0 and Still Faithful 0.2.1, on Pure Faithful 0.1.3.

- The packs now start with every mod on. Each still has its Enabled switch in the settings' Mods menu. The server packages
  are as they were: a server turns on what its owner moves into `mods/`.
- The packs' icons are now the editions' own seals.

## 0.1.0 (2026-11-05)

The first release, a pre-release. Library packs for Old Faithful 0.2.0 and Still Faithful 0.2.1, on Pure Faithful 0.1.3,
with every mod off until you turn it on.

- **World Rules**: rules for each world: Mobs Break Blocks, Keep Inventory, Sleeping, Day Length, Weather, Natural
  Monsters, Monster Light, Apple Drops, Falling Hit Bonus. A world's rules can be locked. Runs where the world runs, so
  a server can use it with any client.
- **Better Dungeons**: beta's dungeons can grow, with corridors to more rooms, in new chunks. Runs where the world runs.
- **Water Bottles**: glass bottles to fill at any water and drink from for nothing. A server running it needs every
  player to have it.
- **Plain F3**: the F3 screen without position, facing and entity counts. Client only.
- **No Toasts**: no achievement or tutorial pop-ups. Client only.
- **Faithful Core**, what the mods build on, and an API for writing more ([docs/library-authoring.md](docs/library-authoring.md)).
