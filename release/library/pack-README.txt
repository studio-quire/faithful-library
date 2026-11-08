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
  Blasts Destroy Items, Keep Inventory, Sleeping, Day Length, Weather, Natural Monsters, Monster Light, Apple Drops, Falling Hit Bonus. A world
  nobody changed plays as beta. Lock Rules locks a world's rules so nothing in the game changes them; taking the line
  rules-locked=true out of the world's faithful-world.properties, with the game closed, unlocks them. While a world's
  rules are locked, Mods... doesn't switch off a mod that has rules.
- Buried Towers: ruined towers stand on the land about every 24 chunks, and go a long way down: floors somebody built
  and time got to, a different way down from each, in beta's own blocks. They take the place of beta's small dungeons in
  new chunks. A world chooses its towers when it's created, with Towers on Create World's World Rules... page: Simple
  (the default), crude stumps of cobble and moss like beta's dungeons, or Complex, big towers of dressed stone. Rooms
  the old Better Dungeons built stay in your worlds as ordinary blocks.
- Water Bottles: glass bottles you fill at any water and drink from for nothing. Three glass in a V at the
  bench makes three. An ingredient for mods to come.
- Hearty Food: food heals half as much again, at half a heart a second instead of at once, and pale hearts show where
  you'll end up. Eating is one click as ever. More food adds to what's coming, a hit doesn't cancel it,
  and it helps even mid-fight. A golden apple still heals everything at once. Food past a full bar is wasted, as in beta.
- Alchemy: gild a rose, then brew it. Four gold ingots around a rose at the bench make a Golden Rose. A water bottle
  with one, two or three of them makes a Weak Health Potion, a Health Potion or a Strong Health Potion, in the
  inventory's grid too. Drinking heals 4, 6 or 8 hearts at once and leaves the bottle; at full health it isn't drunk. It
  needs Water Bottles, which comes on with it.
- Blue Flowers: a blue flower in patches across Taiga and Tundra, as dandelions grow in warmer land. New chunks only.
  It's placed and picked like the rose and dandelion, and makes two light blue dye. Bonemeal on grass in those biomes can
  grow one too.
- Item Repair: two worn tools, weapons or pieces of armour of the same kind in the crafting grid make one, with what
  was left of both, up to as good as new, as in later versions. A world rule, Repair Bonus, off to start with, adds a
  twentieth of a new one's durability on top.
- Plain F3: the F3 screen keeps its frame rate and memory and loses position, facing and entity counts, and mobs lose
  the numbers beta tags them with through walls, so you find your way without coordinates.
- No Toasts: no achievement or tutorial pop-ups. Achievements are still earned.

On a server: World Rules runs where the world runs, so a server can use it with any client, vanilla too. Hearty Food does
too, with food healing over time for everyone there, though only a player who has it too
sees the pale hearts; on a server without it, food is beta's. Item Repair runs where the world runs too: any client can
mend on a server with it, and nothing is mended on one without it. Plain F3 and No Toasts are yours alone and work anywhere. A server running Water Bottles, Alchemy, Blue Flowers or Buried Towers needs every player
to have it: anyone else is refused with a message saying so. If you have any library mod on, the refusal screen's Match
This Server's Mods turns on what that server needs, for your next launch; with none on, turn them on as above.
With them on you can still join servers without them, where they can't be made.
With any library mod on, your login says you have Faithful Core: a server with the core can see that, and which of its
own mod channels you share, but isn't told which other mods you have.

Turning a mod off or removing it:
- Your world opens without it. World Rules' choices stay in the world's file and come back with the mod; the hour may
  jump if you'd changed the day's length. Towers already built stay, as ordinary blocks, and new chunks go back to beta's
  dungeons. Hearty Food's healing still to come waits in your save and goes on if the mod returns. Plain F3 and No Toasts
  leave nothing behind.
- Water Bottles' items don't survive: bottles in that world become bowls for good, the next time you open it. Alchemy
  goes off with it, as it needs it.
- Alchemy's don't either: golden roses become roses and potions become bowls, for good.
- Blue Flowers' don't either: blue flowers, growing or carried, become roses for good.
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
