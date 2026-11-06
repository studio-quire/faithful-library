package org.studioquire.faithful.betterdungeons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.studioquire.oldfaithful.core.api.Chunks;
import org.studioquire.oldfaithful.core.api.Worldgen;

/**
 * One world's dungeons in memory: those waiting to grow, and the grown ones to check, of the chunks that are loaded. What matters is on the chunks, so this is only an index of them.
 * <p>
 * A dungeon grows in a single tick, once every chunk it could reach is loaded and final: no vanilla decoration will ever write those cells again, or read them to make up its mind, so nothing outside
 * the dungeon can be changed by it, and what its layout reads is the same whatever order the chunks came in. Until then it's beta's room, and asks for nothing: the chunks come as players bring them.
 */
public final class Keeper {
	/** Each is let go at its world's unload: a keeper holds its world. */
	private static final Map<World, Keeper> KEEPERS = new HashMap<>();

	private final World world;
	final long seed;
	private final Map<Long, Site> waiting = new LinkedHashMap<>();
	private final Map<Long, Layout> grown = new LinkedHashMap<>();

	private Keeper(World world) {
		this.world = world;
		seed = world.getSeed();
	}

	public static Keeper of(World world) {
		return KEEPERS.computeIfAbsent(world, Keeper::new);
	}

	static void forget(World world) {
		KEEPERS.remove(world);
	}

	/** The one who keeps a world that has anything to keep. */
	static Keeper existing(World world) {
		return KEEPERS.get(world);
	}

	static long chunkKey(int cx, int cz) {
		return (long) cx << 32 | cz & 0xFFFFFFFFL;
	}

	/** Dungeons waiting on chunks that are in memory. */
	public int waiting() {
		return waiting.size();
	}

	private static Kept kept(Chunk chunk) {
		Kept kept = BetterDungeons.kept.get(chunk);
		return kept != null ? kept : new Kept();
	}

	private static void store(Chunk chunk, Kept kept) {
		BetterDungeons.kept.set(chunk, kept.empty() ? null : kept);
	}

	/** A third stay as beta made them, so a big one is a find. */
	public static boolean grows(long seed, int x, int y, int z) {
		return Long.remainderUnsigned(Mix.hash(seed, x, y, z, Mix.SHARE), 3) != 0;
	}

	void placed(Site site) {
		if (!grows(seed, site.x, site.y, site.z)) return;
		Chunk chunk = world.getChunk(site.x >> 4, site.z >> 4);
		Kept kept = kept(chunk);
		kept.waiting.add(site);
		store(chunk, kept);
		waiting.put(site.key(), site);
	}

	void loaded(Chunk chunk, Kept kept) {
		for (Site site : kept.waiting) waiting.put(site.key(), site);
		for (Layout layout : kept.grown) grown.put(layout.site.key(), layout);
	}

	void unloaded(Kept kept) {
		for (Site site : kept.waiting) waiting.remove(site.key());
		for (Layout layout : kept.grown) grown.remove(layout.site.key());
	}

	private boolean loaded(int cx, int cz) {
		return Chunks.isLoaded(world, cx, cz);
	}

	/** A chunk no decoration can touch again: its own and the three that reach into it from the north and west are done. */
	private boolean finished(int cx, int cz) {
		for (int x = cx - 1; x <= cx; x++) {
			for (int z = cz - 1; z <= cz; z++) {
				if (!loaded(x, z) || !world.getChunk(x, z).terrainPopulated) return false;
			}
		}
		return true;
	}

	private boolean ready(Site site) {
		for (int cx = site.x - Layout.REACH >> 4; cx <= site.x + Layout.REACH >> 4; cx++) {
			for (int cz = site.z - Layout.REACH >> 4; cz <= site.z + Layout.REACH >> 4; cz++) {
				if (!finished(cx, cz)) return false;
			}
		}
		return true;
	}

	/** For a test: where a site stands, in words. */
	public String explain(Site site) {
		StringBuilder unready = new StringBuilder();
		for (int cx = site.x - Layout.REACH >> 4; cx <= site.x + Layout.REACH >> 4; cx++) {
			for (int cz = site.z - Layout.REACH >> 4; cz <= site.z + Layout.REACH >> 4; cz++) {
				if (!finished(cx, cz)) unready.append(' ').append(cx).append(',').append(cz).append(loaded(cx, cz) ? world.getChunk(cx, cz).terrainPopulated ? "" : "(unpopulated)" : "(unloaded)");
			}
		}
		String plan = "";
		if (unready.length() == 0) {
			Layout layout = Layout.plan(seed, site, new WorldGround(world, site, this));
			plan = ", would plan " + layout.rooms.size() + " rooms";
		}
		return (waiting.containsKey(site.key()) ? "waiting" : "not waiting") + (unready.length() == 0 ? ", ready" : ", not final:" + unready) + plan;
	}

	public void tick() {
		if (!grown.isEmpty()) checkGrown();
		for (Iterator<Site> i = waiting.values().iterator(); i.hasNext(); ) {
			Site site = i.next();
			if (ready(site)) {
				i.remove();
				grow(site);
				// One a tick: a world's first chunks can bring dozens at once.
				return;
			}
		}
	}

	private void grow(Site site) {
		Chunk home = world.getChunk(site.x >> 4, site.z >> 4);
		Kept kept = kept(home);
		kept.waiting.removeIf(s -> s.key() == site.key());
		store(home, kept);

		WorldGround ground = new WorldGround(world, site, this);
		Layout layout = ground.own;
		if (layout != null) {
			// Grown already, and its home chunk is back from before: only what lacks it is written again.
			build(layout, missing(layout), ground);
			return;
		}
		if (home.getBlockId(site.x & 15, site.y, site.z & 15) != Block.SPAWNER.id) return;
		layout = Layout.plan(seed, site, ground);
		if (layout.rooms.size() > 1) build(layout, layout.chunks(), ground);
	}

	/** The loaded chunks a layout reaches that don't know it. */
	private Set<Long> missing(Layout layout) {
		Set<Long> missing = new TreeSet<>();
		for (long key : layout.chunks()) {
			int cx = (int) (key >> 32), cz = (int) key;
			if (loaded(cx, cz) && !kept(world.getChunk(cx, cz)).holds(layout.site)) missing.add(key);
		}
		return missing;
	}

	private void build(Layout layout, Set<Long> chunks, WorldGround ground) {
		if (chunks.isEmpty()) return;
		// Painted first, so a layout that can't be painted stops before anything is noted or written.
		Cells cells = Furnishing.paint(seed, layout);
		for (long key : chunks) {
			Chunk chunk = world.getChunk((int) (key >> 32), (int) key);
			Kept kept = kept(chunk);
			if (!kept.holds(layout.site)) {
				kept.grown.add(layout);
				store(chunk, kept);
			}
		}
		Mason mason = new Mason(world, seed, ground);
		cells.each((x, y, z, kind) -> {
			if (chunks.contains(chunkKey(x >> 4, z >> 4))) mason.place(x, y, z, kind);
		});
	}

	/**
	 * A dungeon's chunks can come back from different saves: after a crash, a chunk lost to damage comes back as new, and closing single player's window skips beta's last save while autosaves have
	 * written some chunks and not others. Each holds the whole layout, so any of them shows what the others lack, and that's written again only where it's missing, once the rest of its reach is
	 * final, as at its first growth.
	 */
	private void checkGrown() {
		for (Iterator<Layout> i = grown.values().iterator(); i.hasNext(); ) {
			Layout layout = i.next();
			boolean unseen = false;
			for (long key : layout.chunks()) {
				if (!loaded((int) (key >> 32), (int) key)) {
					unseen = true;
					break;
				}
			}
			Set<Long> missing = missing(layout);
			if (missing.isEmpty()) {
				if (!unseen) i.remove();
			} else if (ready(layout.site)) {
				i.remove();
				build(layout, missing, new WorldGround(world, layout.site, this));
			}
		}
	}
}
