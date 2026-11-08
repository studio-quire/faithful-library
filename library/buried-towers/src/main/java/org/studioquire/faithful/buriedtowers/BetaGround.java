package org.studioquire.faithful.buriedtowers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.carver.CaveCarver;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;
import org.studioquire.faithful.buriedtowers.plan.Climate;
import org.studioquire.faithful.buriedtowers.plan.Ground;

/**
 * Beta's terrain worked out on the side: a generator of our own, seeded as the world's is, asked only for the land's shape. It makes no chunk and never calls the generator's {@code getChunk}, so no
 * mod's feature hears of it, and a site's ground is known from any chunk, in any order, before its neighbours exist. The one thing it shares with the world is the biome source, whose fields are
 * scratch space between one call and the next: they're put back as they were. For {@link #surface} it goes on as {@code getChunk} does, the surface and then the caves, with the generator's random
 * seeded for the chunk as there: the surface's sand and gravel aren't cut by a cave, and its water stops one.
 */
final class BetaGround implements Ground {
	/** Enough for a tower's whole square, so a tower reads no chunk twice; chunks are dropped when it's full. */
	private static final int KEPT = 64;

	private final World world;
	private final OverworldChunkGenerator generator;
	private final Random random;
	private final CaveCarver caves = new CaveCarver();
	/** Per chunk, each column's {@link #top} and then its {@link #surface}. */
	private final Map<Long, byte[]> tops = new HashMap<>();

	BetaGround(World world) {
		this.world = world;
		generator = new OverworldChunkGenerator(world, world.getSeed());
		random = generatorRandom(generator);
	}

	/** The generator's own random, its only one, which the surface draws from. */
	private static Random generatorRandom(OverworldChunkGenerator generator) {
		try {
			for (Field f : OverworldChunkGenerator.class.getDeclaredFields()) {
				if (f.getType() != Random.class) continue;
				f.setAccessible(true);
				return (Random) f.get(generator);
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Buried Towers can't reach the terrain generator's random", e);
		}
		throw new IllegalStateException("Buried Towers found no random in the terrain generator");
	}

	@Override
	public int top(int x, int z) {
		return column(x, z, 0);
	}

	@Override
	public int surface(int x, int z) {
		return column(x, z, 256);
	}

	private int column(int x, int z, int which) {
		long key = (long) (x >> 4) << 32 | z >> 4 & 0xFFFFFFFFL;
		byte[] columns = tops.get(key);
		if (columns == null) {
			if (tops.size() >= KEPT) tops.clear();
			columns = land(x >> 4, z >> 4);
			tops.put(key, columns);
		}
		return columns[which + (x & 15) * 16 + (z & 15)] & 0xFF;
	}

	private byte[] land(int chunkX, int chunkZ) {
		BiomeSource source = world.method_1781();
		double[] temperature = source.temperatureMap, downfall = source.downfallMap, weirdness = source.weirdnessMap;
		Biome[] biomes = source.biomes;
		try {
			source.temperatureMap = null;
			source.downfallMap = null;
			source.weirdnessMap = null;
			Biome[] here = source.getBiomesInArea(null, chunkX * 16, chunkZ * 16, 16, 16);
			byte[] blocks = new byte[32768];
			generator.buildTerrain(chunkX, chunkZ, blocks, here, source.temperatureMap);
			byte[] columns = new byte[512];
			int stone = Block.STONE.id;
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					int y = 127;
					int base = x << 11 | z << 7;
					while (y > 0 && blocks[base + y] != stone) y--;
					columns[x * 16 + z] = (byte) (y + 1);
				}
			}
			random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
			generator.buildSurfaces(chunkX, chunkZ, blocks, here);
			caves.carve(generator, world, chunkX, chunkZ, blocks);
			int water = Block.WATER.id, flowing = Block.FLOWING_WATER.id;
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					int y = 127;
					int base = x << 11 | z << 7;
					while (y > 0 && (blocks[base + y] == 0 || blocks[base + y] == water || blocks[base + y] == flowing)) y--;
					columns[256 + x * 16 + z] = (byte) (y + 1);
				}
			}
			return columns;
		} finally {
			source.temperatureMap = temperature;
			source.downfallMap = downfall;
			source.weirdnessMap = weirdness;
			source.biomes = biomes;
		}
	}

	Climate climate(int x, int z) {
		BiomeSource source = world.method_1781();
		double[] temperature = source.temperatureMap, downfall = source.downfallMap, weirdness = source.weirdnessMap;
		Biome[] biomes = source.biomes;
		try {
			source.temperatureMap = null;
			source.downfallMap = null;
			source.weirdnessMap = null;
			Biome biome = source.getBiomesInArea(null, x, z, 1, 1)[0];
			if (biome == Biome.DESERT) return Climate.DESERT;
			if (biome == Biome.TAIGA || biome == Biome.TUNDRA || biome == Biome.ICE_DESERT) return Climate.COLD;
			if (biome == Biome.SWAMPLAND || biome == Biome.RAINFOREST) return Climate.WET;
			if (biome == Biome.FOREST || biome == Biome.SEASONAL_FOREST) return Climate.WOODED;
			return Climate.OPEN;
		} finally {
			source.temperatureMap = temperature;
			source.downfallMap = downfall;
			source.weirdnessMap = weirdness;
			source.biomes = biomes;
		}
	}
}
