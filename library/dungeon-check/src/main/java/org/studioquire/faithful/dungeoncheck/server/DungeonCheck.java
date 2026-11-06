package org.studioquire.faithful.dungeoncheck.server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.studioquire.oldfaithful.core.api.Commands;
import org.studioquire.oldfaithful.core.api.FaithfulMod;
import org.studioquire.oldfaithful.core.api.Mod;
import org.studioquire.oldfaithful.core.api.Server;
import org.studioquire.oldfaithful.core.api.Ticks;
import org.studioquire.oldfaithful.core.api.Worldgen;

/**
 * Real generation by a script, for {@code :harness:betterDungeonsTest}: chunks of a square far from the spawn loaded in a chosen order, some a tick, as a player's walk would bring them but in any
 * order we like, then every cell of the square written to a file. With or without Better Dungeons, which this reaches only through {@link Survey}.
 */
public final class DungeonCheck implements FaithfulMod {
	private static final String DONE = "[dungeon-check] DONE ";
	private static final boolean GROWING = FabricLoader.getInstance().isModLoaded("better-dungeons");

	private final List<int[]> placed = new ArrayList<>();
	private final Map<Long, int[]> seen = new LinkedHashMap<>();
	private List<int[]> queue = new ArrayList<>();
	private int perTick, position;
	private int settled = -1, lastWaiting, quiet;

	@Override
	public void init(Mod mod) {
		Commands.register("dungeontest", "Better Dungeons' test driver", (args, reply) -> {
			try {
				command(args);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		Worldgen.onDungeon(d -> {
			if (d.world().dimension.id == Worldgen.OVERWORLD) placed.add(new int[] {d.x(), d.y(), d.z(), d.halfX(), d.halfZ()});
		});
		Ticks.onWorld(this::tick);
	}

	private static List<int[]> order(int x0, int z0, int size, String order) {
		List<int[]> chunks = new ArrayList<>();
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) chunks.add(new int[] {x0 + x, z0 + z});
		}
		if (order.equals("back")) {
			Collections.reverse(chunks);
		} else if (order.startsWith("shuffle")) {
			Collections.shuffle(chunks, new Random(Long.parseLong(order.substring(7))));
		} else if (order.equals("rings")) {
			double c = (size - 1) / 2.0;
			chunks.sort((a, b) -> Double.compare(Math.max(Math.abs(a[0] - x0 - c), Math.abs(a[1] - z0 - c)), Math.max(Math.abs(b[0] - x0 - c), Math.abs(b[1] - z0 - c))));
		} else if (!order.equals("rows")) {
			throw new IllegalArgumentException("No such order: " + order);
		}
		return chunks;
	}

	/** {@code gen x0 z0 size order perTick [count]}, {@code settle}, {@code spawn}, {@code revert}, {@code crash partialSaves}, {@code dump x0 z0 size file}. */
	private void command(String[] args) throws IOException {
		switch (args[0]) {
			case "gen": {
				queue = order(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
				perTick = Integer.parseInt(args[5]);
				if (args.length > 6) queue = new ArrayList<>(queue.subList(0, Integer.parseInt(args[6])));
				position = 0;
				break;
			}
			case "settle":
				settled = 0;
				quiet = 0;
				break;
			case "spawn": {
				Vec3i spawn = Server.world(Worldgen.OVERWORLD).getSpawnPos();
				System.out.println(DONE + "spawn " + spawn.x + " " + spawn.z);
				break;
			}
			case "revert":
				System.out.println(DONE + "revert " + Survey.revert(Server.world(Worldgen.OVERWORLD)));
				break;
			case "crash": {
				// A few of the game's own partial saves, as an autosave leaves it, and then gone with no goodbye.
				ServerWorld world = (ServerWorld) Server.world(Worldgen.OVERWORLD);
				for (int i = 0; i < Integer.parseInt(args[1]); i++) world.chunkCache.save(false, null);
				Runtime.getRuntime().halt(1);
				break;
			}
			case "dump":
				dump(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
				System.out.println(DONE + "dump");
				break;
			default:
				throw new IllegalArgumentException(args[0]);
		}
	}

	private void tick(World world) {
		if (world.dimension.id != Worldgen.OVERWORLD) return;
		if (position < queue.size()) {
			for (int i = 0; i < perTick && position < queue.size(); i++, position++) {
				int[] chunk = queue.get(position);
				Chunk loaded = ((ServerWorld) world).chunkCache.loadChunk(chunk[0], chunk[1]);
				if (GROWING) Survey.LOADED.add(loaded);
			}
			if (position == queue.size()) System.out.println(DONE + "gen");
		}
		if (settled >= 0) {
			int waiting = GROWING ? Survey.waiting(world) : 0;
			quiet = waiting == lastWaiting ? quiet + 1 : 0;
			lastWaiting = waiting;
			// Long enough for everything queued to have been built, and for the world's own light to catch up.
			if (quiet >= 60) {
				settled = -1;
				System.out.println(DONE + "settle waiting=" + waiting);
			}
		}
	}

	private void dump(int x0, int z0, int size, String file) throws IOException {
		World world = Server.world(Worldgen.OVERWORLD);
		Files.createDirectories(Paths.get(file).toAbsolutePath().getParent());
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(file)), 1 << 16))) {
			out.writeInt(x0);
			out.writeInt(z0);
			out.writeInt(size);
			byte[] cells = new byte[32768];
			for (int cx = x0; cx < x0 + size; cx++) {
				for (int cz = z0; cz < z0 + size; cz++) {
					Chunk chunk = world.getChunk(cx, cz);
					out.writeBoolean(chunk.terrainPopulated);
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 128; y++) cells[x << 11 | z << 7 | y] = (byte) chunk.getBlockId(x, y, z);
						}
					}
					out.write(cells);
				}
			}
			out.writeInt(placed.size());
			for (int[] site : placed) {
				for (int v : site) out.writeInt(v);
			}
			if (GROWING) Survey.write(world, x0, z0, size, out);
			else out.writeInt(0);
		}
	}
}
