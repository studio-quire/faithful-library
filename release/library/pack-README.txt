@EDITION_NAME@ @EDITION_VERSION@ with the Faithful Library @VERSION@

This folder is a Prism Launcher instance. Prism downloads Minecraft beta 1.7.3 itself, from Mojang, the first time
you launch it; everything else is already here. You need a Minecraft account that owns the game.

It's @EDITION_NAME@'s own pack with the Faithful Library's mods in .minecraft/mods-library, all on.
@EDITION_LINE@ is what's underneath.

Turning a mod off or on: in the game, Options > @EDITION_NAME@... > Mods... lists them. Each has a page with an Enabled
switch, which takes effect the next time you launch. Faithful Core, which the mods build on, comes on by itself when any
mod is on and goes off when none is. With all of them off the pack plays exactly as @EDITION_NAME@ does: none of the
library's code is loaded, and nothing new goes over the network. The switches are kept in
.minecraft/mods-library/selection.txt.

The mods:
- World Rules: rules for each world, on its page in Mods... and on Create World's World Rules... page: Mobs Break Blocks,
  Keep Inventory, Sleeping, Day Length, Weather, Natural Monsters, Monster Light, Apple Drops, Falling Hit Bonus. A world
  nobody changed plays as beta. Lock Rules locks a world's rules so nothing in the game changes them; taking the line
  rules-locked=true out of the world's faithful-world.properties, with the game closed, unlocks them.
- Better Dungeons: beta's dungeons can grow, with corridors to more rooms, buried and sealed, in beta's own blocks,
  monsters and chest list. New chunks only.
- Water Bottles: glass bottles you fill at any water and drink from for nothing. Three glass in a V at the bench makes
  three. An ingredient for mods to come.
- Plain F3: the F3 screen keeps its frame rate and memory and loses position, facing and entity counts, so you find your
  way without coordinates.
- No Toasts: no achievement or tutorial pop-ups. Achievements are still earned.

On a server: World Rules and Better Dungeons run where the world runs, so a server can use them with any client,
vanilla too. Plain F3 and No Toasts are yours alone and work anywhere. A server running Water Bottles needs every player
to have it: anyone else is refused with a message saying so. If you have any library mod on, the refusal screen's Use
This Server's Set turns on what that server needs, for your next launch; with none on, turn on Water Bottles as above.
With Water Bottles on you can still join servers without it, where the bottles can't be made.
With any library mod on, your login says you have Faithful Core: a server with the core can see that, and which of its
own mod channels you share, but isn't told which other mods you have.

Turning a mod off or removing it:
- Your world opens without it. World Rules' choices stay in the world's file and come back with the mod; the hour may
  jump if you'd changed the day's length. Rooms Better Dungeons has built stay; a dungeon it hadn't reached stays beta's
  room. Plain F3 and No Toasts leave nothing behind.
- Water Bottles' items don't survive: bottles in that world become bowls for good, the next time you open it.
- If other library mods are still on, a screen tells you before such a world opens, and offers to make a backup. If that
  was the last mod on, Faithful Core goes too and nothing warns you, so back up first: copy the world's folder from
  .minecraft/saves. With the core gone, the library's data in a chunk goes when that chunk is next saved.

Your worlds are in .minecraft/saves, in beta 1.7.3's own format with the library's data beside it: they open in the
original game and in every edition, as if the mods had been removed.

If something goes wrong, the game's log is .minecraft/logs/purefaithful/latest.log.

Made by Claude Opus 5.5, an AI model by Anthropic, at Studio Quire. Idea, direction and play-testing: Shoalstone.
Open source under the MIT licence. Software by others included here, and its licences: THIRD-PARTY-NOTICES.txt,
licenses/ and sources/.
@HOMEPAGE@
Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
