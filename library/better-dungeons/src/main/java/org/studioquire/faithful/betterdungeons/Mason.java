package org.studioquire.faithful.betterdungeons;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.studioquire.oldfaithful.core.api.Worldgen;

/**
 * Puts a painted dungeon's cells in the world. Never notifies neighbours, as generation doesn't, so nothing beside the rooms falls or flows; players' clients and the renderer do hear. Writing a cell
 * again changes nothing, and a cell keeps whatever outranks what's asked for, so this can run twice, and in any order with another dungeon.
 */
final class Mason {
	private final World world;
	private final long seed;
	private final WorldGround ground;

	Mason(World world, long seed, WorldGround ground) {
		this.world = world;
		this.seed = seed;
		this.ground = ground;
	}

	/** What's in a cell, by rank. Gravel in the rock is only rock. */
	private byte present(int id, int x, int y, int z) {
		if (id == 0) return Cells.AIR;
		if (id == Block.COBBLESTONE.id) return Cells.COBBLE;
		if (id == Block.MOSSY_COBBLESTONE.id) return Cells.MOSS;
		if (id == Block.GRAVEL.id) return ground.theirs(x, y, z) ? Cells.GRAVEL : Cells.NONE;
		if (id == Block.WATER.id) return Cells.WATER;
		if (id == Block.COBWEB.id) return Cells.WEB;
		if (id == Block.SPAWNER.id) return Cells.SPAWNER;
		if (id == Block.CHEST.id) return Cells.CHEST;
		return Cells.NONE;
	}

	private static int block(byte kind) {
		switch (kind) {
			case Cells.COBBLE: return Block.COBBLESTONE.id;
			case Cells.MOSS: return Block.MOSSY_COBBLESTONE.id;
			case Cells.AIR: return 0;
			case Cells.GRAVEL: return Block.GRAVEL.id;
			case Cells.WATER: return Block.WATER.id;
			case Cells.WEB: return Block.COBWEB.id;
			case Cells.SPAWNER: return Block.SPAWNER.id;
			case Cells.CHEST: return Block.CHEST.id;
			default: throw new IllegalArgumentException("No block for a cell of kind " + kind);
		}
	}

	void place(int x, int y, int z, byte kind) {
		int id = world.getBlockId(x, y, z);
		byte present = present(id, x, y, z);
		if (kind == Cells.CEILING) {
			if (present != Cells.NONE || id != Block.GRAVEL.id && id != Block.SAND.id) return;
			kind = Cells.COBBLE;
		} else if (kind <= present) {
			return;
		}
		world.setBlockWithoutNotifyingNeighbors(x, y, z, block(kind));
		world.blockUpdateEvent(x, y, z);
		BlockEntity entity = kind >= Cells.SPAWNER ? world.getBlockEntity(x, y, z) : null;
		if (entity instanceof ChestBlockEntity) fill((ChestBlockEntity) entity, x, y, z);
		else if (entity instanceof MobSpawnerBlockEntity) ((MobSpawnerBlockEntity) entity).setSpawnedEntityId(Furnishing.monster(seed, x, y, z));
	}

	/** As beta fills one, from its own list: eight draws, each into any slot. */
	private void fill(ChestBlockEntity chest, int x, int y, int z) {
		Random random = Mix.random(seed, x, y, z, Mix.CHEST);
		for (int i = 0; i < 8; i++) {
			ItemStack stack = Worldgen.dungeonChestItem(random);
			if (stack != null) chest.setStack(random.nextInt(chest.size()), stack);
		}
	}
}
