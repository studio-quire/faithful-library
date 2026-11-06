package org.studioquire.oldfaithful.core.api;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;

/**
 * Explosions, where they run (never on a client on a server). Listeners are a filter: each sees what the last left. A
 * listener runs after the blast's rays are cast, before fires are set and blocks broken.
 */
public final class Explosions {
	public enum Cause {
		CREEPER(true), FIREBALL(true), TNT(false), BED(false), OTHER(false);

		private final boolean mob;

		Cause(boolean mob) {
			this.mob = mob;
		}

		public boolean isMob() {
			return mob;
		}
	}

	public static final class BlockEvent {
		private final Explosion explosion;
		private final Cause cause;
		private final net.minecraft.world.World world;

		public BlockEvent(Explosion explosion, net.minecraft.world.World world, Cause cause) {
			this.explosion = explosion;
			this.world = world;
			this.cause = cause;
		}

		public Cause cause() {
			return cause;
		}

		public net.minecraft.world.World world() {
			return world;
		}

		public double x() {
			return explosion.x;
		}

		public double y() {
			return explosion.y;
		}

		public double z() {
			return explosion.z;
		}

		/**
		 * The blast harms things as usual but breaks and burns nothing. Air stays in the list: the game draws its smoke from
		 * it, on clients too.
		 */
		@SuppressWarnings("unchecked")
		public void spareBlocks() {
			explosion.fire = false;
			for (Iterator<BlockPos> i = ((Set<BlockPos>) explosion.damagedBlocks).iterator(); i.hasNext(); ) {
				BlockPos p = i.next();
				if (world.getBlockId(p.x, p.y, p.z) != 0) i.remove();
			}
		}
	}

	private static final Listeners<Consumer<BlockEvent>> BLOCKS = new Listeners<>("Explosions.onBlocks");

	private Explosions() {
	}

	public static void onBlocks(Consumer<BlockEvent> listener) {
		BLOCKS.add(listener);
	}

	/** The core's. */
	public static void fire(BlockEvent event) {
		BLOCKS.each(l -> l.accept(event));
	}

	public static boolean hasListeners() {
		return !BLOCKS.isEmpty();
	}
}
