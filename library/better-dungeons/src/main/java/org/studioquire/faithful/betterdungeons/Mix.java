package org.studioquire.faithful.betterdungeons;

import java.util.Random;

/** The mod's own randomness, from the world's seed and a place: nothing of it comes near vanilla's streams. */
final class Mix {
	static final int LAYOUT = 1, FURNISHING = 2, SHARE = 3, MOSS = 4, CHEST = 5, MONSTER = 6;

	private Mix() {
	}

	static long hash(long seed, int x, int y, int z, int salt) {
		long h = seed + salt * 0x9E3779B97F4A7C15L;
		h = (h ^ x * 341873128712L) * 0xBF58476D1CE4E5B9L;
		h = (h ^ h >>> 29 ^ z * 132897987541L) * 0x94D049BB133111EBL;
		h = (h ^ h >>> 32 ^ y * 0x2545F4914F6CDD1DL) * 0xBF58476D1CE4E5B9L;
		return h ^ h >>> 31;
	}

	static Random random(long seed, int x, int y, int z, int salt) {
		return new Random(hash(seed, x, y, z, salt));
	}
}
