package org.studioquire.faithful.betterdungeons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.chunk.Chunk;
import org.studioquire.oldfaithful.core.api.Attachment;
import org.studioquire.oldfaithful.core.api.Chunks;
import org.studioquire.oldfaithful.core.api.FaithfulMod;
import org.studioquire.oldfaithful.core.api.Lifecycle;
import org.studioquire.oldfaithful.core.api.Mod;
import org.studioquire.oldfaithful.core.api.Ticks;
import org.studioquire.oldfaithful.core.api.Worldgen;

/** Beta's dungeons grown into a few rooms, in the world's own blocks, on the side that runs the world and nowhere else: {@link Keeper} has how. No content, so a server asks nothing of its clients. */
public final class BetterDungeons implements FaithfulMod {
	public static Attachment<Chunk, Kept> kept;

	@Override
	public void init(Mod mod) {
		kept = mod.attachment("dungeons", Attachment.chunk(Kept.CODEC)).persist();
		Worldgen.onDungeon(d -> {
			if (d.world().dimension.id == Worldgen.OVERWORLD) Keeper.of(d.world()).placed(new Site(d.x(), d.y(), d.z(), d.halfX(), d.halfZ()));
		});
		Chunks.onLoaded(chunk -> {
			Kept k = kept.get(chunk);
			if (k != null) Keeper.of(chunk.world).loaded(chunk, k);
		});
		Chunks.onUnloaded(chunk -> {
			Kept k = kept.get(chunk);
			Keeper keeper = Keeper.existing(chunk.world);
			if (k != null && keeper != null) keeper.unloaded(k);
		});
		Ticks.onWorld(world -> {
			Keeper keeper = Keeper.existing(world);
			if (keeper != null) keeper.tick();
		});
		Lifecycle.onWorldUnload(Keeper::forget);
	}

	/** What's grown through a chunk, for a test to look at. */
	public static List<Layout> grownIn(Chunk chunk) {
		Kept k = kept.get(chunk);
		return k == null ? new ArrayList<>() : k.grown;
	}
}
