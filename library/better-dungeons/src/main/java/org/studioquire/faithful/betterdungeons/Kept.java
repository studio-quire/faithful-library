package org.studioquire.faithful.betterdungeons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.studioquire.oldfaithful.core.api.Codec;

/**
 * What a chunk keeps for the mod: dungeons in it still waiting to grow, under their spawner's chunk, and every grown dungeon that reaches it. A dungeon is written into each chunk it touches, so any
 * one of them is enough to tell if another went back to an older save.
 */
public final class Kept {
	final List<Site> waiting = new ArrayList<>();
	final List<Layout> grown = new ArrayList<>();

	static final Codec<Kept> CODEC = Codec.of(Kept::write, Kept::read);

	boolean empty() {
		return waiting.isEmpty() && grown.isEmpty();
	}

	boolean holds(Site site) {
		for (Layout layout : grown) {
			if (layout.site.key() == site.key()) return true;
		}
		return false;
	}

	/**
	 * A layout is only rooms and corridors, and what's in them follows from {@link Furnishing} and {@link Mix}, so those are part of the format: a change in how they draw is a new version here, and a
	 * layout of an older one has to be painted as it was.
	 */
	static final byte VERSION = 1;

	private static NbtCompound write(Kept kept) {
		NbtCompound nbt = new NbtCompound();
		nbt.putByte("V", VERSION);
		NbtList waiting = new NbtList();
		for (Site site : kept.waiting) waiting.add(Layout.writeSite(site));
		nbt.put("Waiting", waiting);
		NbtList grown = new NbtList();
		for (Layout layout : kept.grown) grown.add(layout.write());
		nbt.put("Grown", grown);
		return nbt;
	}

	private static Kept read(NbtCompound nbt) {
		if (nbt.getByte("V") != VERSION) throw new IllegalArgumentException("Better Dungeons' data of version " + nbt.getByte("V") + ", and this is " + VERSION);
		Kept kept = new Kept();
		NbtList waiting = nbt.getList("Waiting");
		for (int i = 0; i < waiting.size(); i++) kept.waiting.add(Layout.readSite((NbtCompound) waiting.get(i)));
		NbtList grown = nbt.getList("Grown");
		for (int i = 0; i < grown.size(); i++) kept.grown.add(Layout.read((NbtCompound) grown.get(i)));
		return kept;
	}
}
