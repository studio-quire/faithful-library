package org.studioquire.faithful.betterdungeons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;

/** The cells round a site, which the caller has found loaded and final. Cells of another grown dungeon read as what they were before it: it found them diggable, or it couldn't have gone there. */
final class WorldGround implements Ground {
	private final World world;
	private final List<Layout> others = new ArrayList<>();
	private final List<Cells> painted = new ArrayList<>();
	/** This dungeon, if it's already grown in a chunk here: a chunk it reached went back to an older save. */
	Layout own;

	WorldGround(World world, Site site, Keeper keeper) {
		this.world = world;
		Set<Long> keys = new HashSet<>();
		for (int cx = site.x - Layout.REACH >> 4; cx <= site.x + Layout.REACH >> 4; cx++) {
			for (int cz = site.z - Layout.REACH >> 4; cz <= site.z + Layout.REACH >> 4; cz++) {
				Kept kept = BetterDungeons.kept.get(world.getChunk(cx, cz));
				if (kept == null) continue;
				for (Layout layout : kept.grown) {
					if (layout.site.key() == site.key()) own = layout;
					else if (keys.add(layout.site.key())) {
						others.add(layout);
						painted.add(Furnishing.paint(keeper.seed, layout));
					}
				}
			}
		}
	}

	private int blockId(int x, int y, int z) {
		return world.getChunk(x >> 4, z >> 4).getBlockId(x & 15, y, z & 15);
	}

	/** Whether a cell is another dungeon's. */
	boolean theirs(int x, int y, int z) {
		for (Cells cells : painted) {
			if (cells.get(x, y, z) != Cells.NONE) return true;
		}
		return false;
	}

	private static boolean diggable(int id) {
		return id == Block.STONE.id || id == Block.DIRT.id || id == Block.GRAVEL.id || id == Block.SAND.id || id == Block.SANDSTONE.id || id == Block.CLAY.id || id == Block.COAL_ORE.id
				|| id == Block.IRON_ORE.id || id == Block.GOLD_ORE.id || id == Block.DIAMOND_ORE.id || id == Block.LAPIS_ORE.id || id == Block.REDSTONE_ORE.id || id == Block.LIT_REDSTONE_ORE.id;
	}

	@Override
	public int at(int x, int y, int z) {
		if (y < 0 || y >= 128) return OPEN;
		// Never what's there now: whether another has grown yet goes by the order chunks came in.
		for (int i = 0; i < painted.size(); i++) {
			if (painted.get(i).get(x, y, z) != Cells.NONE) return others.get(i).site.holds(x, y, z) ? FIRM : DIGGABLE;
		}
		int id = blockId(x, y, z);
		if (falls(id)) return loose(x, y, z) ? OPEN : DIGGABLE;
		if (diggable(id)) return DIGGABLE;
		return solid(id) ? FIRM : OPEN;
	}

	private static boolean falls(int id) {
		return id == Block.GRAVEL.id || id == Block.SAND.id;
	}

	private static boolean solid(int id) {
		return id != 0 && Block.BLOCKS[id] != null && Block.BLOCKS_OPAQUE[id];
	}

	/** Gravel or sand in a column open above or below: when it fell, if it has, goes by the order chunks came in, so here it counts as the gap it may yet leave or have left. */
	private boolean loose(int x, int y, int z) {
		int below = y, above = y;
		while (below > 0 && falls(blockId(x, below, z))) below--;
		while (above < 127 && falls(blockId(x, above, z))) above++;
		return !solid(blockId(x, below, z)) || !solid(blockId(x, above, z));
	}

	@Override
	public boolean air(int x, int y, int z) {
		return blockId(x, y, z) == 0;
	}
}
