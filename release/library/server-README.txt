@EDITION_NAME@ server with the Faithful Library @VERSION@
=========================================================

@EDITION_NAME@'s server package, with the Faithful Library's server-side mods in mods/, all on, as they are in the
library packs: Minecraft beta 1.7.3's own server, run through Fabric Loader with our @SERVER_MODS@ mods, from
@EDITION_LINE@, and the library. Water Bottles is on, so as it comes this server refuses anyone without it, vanilla clients
and the plain editions' packs included; the library packs have it. Below is how to take it out.

From the editions: it's hardened against malicious clients, a crash or power cut can't damage the world, and beta's clear
mistakes are fixed, each on a switch in config/. @EDITION_NAME@'s own README says more: https://github.com/@EDITION_REPO@

What it can't change, because it's how beta 1.7.3's online mode works: a server a player joins could pass that
player's login on to another server, so online mode isn't proof against a hostile server operator. Players should
only join servers they trust with their name, as with vanilla.

Setting up
1. Install Java 21 or newer.
2. Put Minecraft beta 1.7.3's server jar in this folder, named minecraft_server.jar. Mojang no longer offers it
   for download, so use a copy you have; the start script checks it's the genuine one (SHA-1 @SERVER_SHA1@).
   It isn't included: it's Mojang's.
3. Run start.bat (Windows) or start.sh (macOS, Linux). The first start writes server.properties and the world, as
   vanilla does. JAVA picks the java executable and JAVA_OPTS its options (default -Xmx1G).

Turning mods off
With the server stopped, move the jar out of mods/ into any other folder. faithful-core-@VERSION@.jar and
oldfaithful-settings-@FIXED_VERSION@.jar stay as long as any library mod does. Do this before the first start for any mod
you don't want: a world that has seen Water Bottles is asked about below. With all of them out, the core and settings too,
it's the plain @EDITION_NAME@ server: the library's code isn't loaded and nothing new goes over the network.

- world-rules: rules for each world, the same for everyone on the server. /rule <name> <value> for ops and the console,
  or the world's faithful-world.properties. /rule lock locks them until the line rules-locked=true comes out of that
  file, with the server stopped. Any client can join, vanilla too; a client with World Rules shows the server's values.
- better-dungeons: beta's dungeons can grow, with corridors to more rooms, in new chunks. Any client can join.
- water-bottles: every player needs it. A server running it refuses anyone without it, vanilla clients included, with a
  message saying so. A player on a library pack with any mod on gets a button to turn on what your server needs.

Plain F3 and No Toasts are client-only, so they aren't here, and players can use them on any server.

Taking a mod out again
A server refuses to start when a world has seen Water Bottles and it's gone, and says how to go on: make a backup, then
add accept-missing-content=true to the world's faithful-world.properties. Its bottles then become bowls for good. That
line is good for one start: the server sets it back to false, so a later removal is refused again. World Rules' values
stay in that file and come back with the mod. Rooms Better Dungeons has built stay. If you take out
faithful-core too, the library's data in a chunk goes when that chunk is next saved, and nothing checks first.

A vanilla beta 1.7.3 server's world and server.properties work as they are: copy them in. The world stays in beta's
format, with the library's data beside it, which vanilla ignores.

@HOMEPAGE@
Open source under the MIT licence, made by Claude Opus 5.5 (Anthropic's AI model) at Studio Quire.
Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
