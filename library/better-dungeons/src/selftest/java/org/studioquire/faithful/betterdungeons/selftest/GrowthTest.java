package org.studioquire.faithful.betterdungeons.selftest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.DungeonFeature;
import org.studioquire.faithful.betterdungeons.BetterDungeons;
import org.studioquire.faithful.betterdungeons.Cells;
import org.studioquire.faithful.betterdungeons.Furnishing;
import org.studioquire.faithful.betterdungeons.Keeper;
import org.studioquire.faithful.betterdungeons.Layout;
import org.studioquire.faithful.betterdungeons.Site;
import org.studioquire.faithful.harness.test.WorldTest;
import org.studioquire.oldfaithful.core.api.Chunks;

/**
 * A dungeon grown under the plot, with a client joined where there is one, so its cells reach the client as a live change, and then back from a save. Generation itself, in real terrain and any
 * order, is {@code :harness:betterDungeonsTest}'s; this is the rock made solid, beta's own feature run in it, and the hook, the wait, the chunks' records and the block entities from there on.
 */
public final class GrowthTest extends WorldTest {
	/** The rock made solid round the site: all a layout reads, and no further, as a server's view of three chunks makes everything this near final. */
	private static final int FLOOR = 24, BOX = Layout.REACH + 2;
	private static final List<String> MONSTERS = Arrays.asList("Zombie", "Skeleton", "Spider");

	/**
	 * The spawner's cell: near the middle of the plot's chunk, the first there of those that grow, so the box round it stays two chunks each way of the player's. The client works it out too.
	 */
	public static int[] site(long seed, int x, int z) {
		int sz = (z & ~15) + 8;
		for (int i = (x & ~15) + 6; ; i++) {
			if (Keeper.grows(seed, i, FLOOR, sz)) return new int[] {i, FLOOR, sz};
		}
	}

	/**
	 * Whether every chunk the box touches is made and decorated, and the chunks that decorate into it too: a server reads a chunk it hasn't loaded as empty and forgets what's written there, and a
	 * decoration that came later would put a lake or a cave's lava in the box.
	 */
	private boolean settled(int[] site) {
		for (int cx = (site[0] - BOX >> 4) - 1; cx <= site[0] + BOX >> 4; cx++) {
			for (int cz = (site[2] - BOX >> 4) - 1; cz <= site[2] + BOX >> 4; cz++) {
				if (!Chunks.isLoaded(world, cx, cz) || !world.getChunk(cx, cz).terrainPopulated) return false;
			}
		}
		return true;
	}

	/** A stream whose first two draws make beta's biggest room, so the one cave cell below is on its wall. */
	private static Random widest() {
		for (long seed = 0; ; seed++) {
			Random probe = new Random(seed);
			if (probe.nextInt(2) == 1 && probe.nextInt(2) == 1) return new Random(seed);
		}
	}

	private Chunk chunkAt(int[] site) {
		return world.getChunk(site[0] >> 4, site[2] >> 4);
	}

	/** Its getter is the client's alone. */
	private static String monster(BlockEntity spawner) {
		NbtCompound nbt = new NbtCompound();
		spawner.writeNbt(nbt);
		return nbt.getString("EntityId");
	}

	/** Ours: a real one nearby can reach the same chunk. */
	private Layout grown(int[] site) {
		for (Layout layout : BetterDungeons.grownIn(chunkAt(site))) {
			if (layout.site.x == site[0] && layout.site.y == site[1] && layout.site.z == site[2]) return layout;
		}
		return null;
	}

	private void inspect(String when, int[] site) {
		Layout layout = grown(site);
		check(when + ": the chunk knows the dungeon", layout != null);
		if (layout == null) return;
		check(when + ": the hook read beta's room right: " + layout.site.halfX + " by " + layout.site.halfZ, layout.site.halfX == 3 && layout.site.halfZ == 3);
		check(when + ": three rooms or more in solid rock: " + layout.rooms.size(), layout.rooms.size() >= 3 && layout.rooms.size() <= 6);
		long seed = world.getSeed();
		int[] wrong = new int[1], spawners = new int[1], chests = new int[1];
		List<String> faults = new ArrayList<>();
		Furnishing.paint(seed, layout).each((x, y, z, kind) -> {
			int id = world.getBlockId(x, y, z);
			if (kind == Cells.AIR && id != 0 && id != Block.TORCH.id || kind == Cells.GRAVEL && id != Block.GRAVEL.id || kind == Cells.COBBLE && id != Block.COBBLESTONE.id) wrong[0]++;
			if (kind == Cells.SPAWNER) {
				BlockEntity entity = world.getBlockEntity(x, y, z);
				spawners[0]++;
				if (!(entity instanceof MobSpawnerBlockEntity) || !MONSTERS.contains(monster(entity))) faults.add("a spawner at " + x + "," + y + "," + z);
			}
			if (kind == Cells.CHEST) {
				chests[0]++;
				if (!(world.getBlockEntity(x, y, z) instanceof ChestBlockEntity)) faults.add("a chest at " + x + "," + y + "," + z);
			}
		});
		check(when + ": every cell as planned: " + wrong[0] + " wrong", wrong[0] == 0);
		check(when + ": block entities behind them all, " + spawners[0] + " spawners and " + chests[0] + " chests " + faults, faults.isEmpty() && chests[0] > 0);
	}

	@Override
	protected void script() {
		int[] site = site(world.getSeed(), x, z);
		scene("light", args -> light(site));
		scene("visit", args -> player.setPosition(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])));
		if (restarted()) {
			inspect("after a restart", site);
			return;
		}
		until("the chunks round the site are made", 600, () -> settled(site));
		then(() -> {
			for (int cx = site[0] - BOX; cx <= site[0] + BOX; cx++) {
				for (int cz = site[2] - BOX; cz <= site[2] + BOX; cz++) {
					for (int cy = FLOOR - 8; cy <= FLOOR + 12; cy++) world.setBlockWithoutNotifyingNeighbors(cx, cy, cz, Block.STONE.id);
				}
			}
			world.setBlockWithoutNotifyingNeighbors(site[0] + 4, FLOOR, site[2], 0);
			world.setBlockWithoutNotifyingNeighbors(site[0] + 4, FLOOR + 1, site[2], 0);
			check("beta's own feature places its dungeon", new DungeonFeature().generate(world, widest(), site[0], site[1], site[2]));
			check("it's beta's room for now", world.getBlockId(site[0], site[1], site[2]) == Block.SPAWNER.id);
		});
		until("the dungeon grows", 400, () -> grown(site) != null);
		then(() -> {
			if (grown(site) == null) System.out.println("[better-dungeons-selftest] the site at " + site[0] + "," + site[1] + "," + site[2] + ": " + Keeper.of(world).explain(new Site(site[0], site[1], site[2], 3, 3)));
		});
		then(() -> inspect("grown", site));
		// Past a cycle of the game's ticks, so a corridor or a room that was going to fall or flow has.
		wait(60);
		then(() -> inspect("a while later", site));
	}

	/** Torches along the rooms' walls, for a picture. */
	private void light(int[] site) {
		for (Layout.Room room : grown(site).rooms) {
			if (world.getBlockId(room.x0, FLOOR, room.z0) == 0) world.setBlock(room.x0, FLOOR, room.z0, Block.TORCH.id);
			if (world.getBlockId(room.x1, FLOOR, room.z1) == 0) world.setBlock(room.x1, FLOOR, room.z1, Block.TORCH.id);
		}
	}
}
