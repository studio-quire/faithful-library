package org.studioquire.faithful.betterdungeons;

/**
 * A dungeon cell by cell, in a box round its spawner. Each kind of cell has a rank and the higher wins, in painting here and on the world, so where two dungeons reach the same cell it doesn't matter
 * which was first. Beta's room isn't in it, except for the doors in its walls.
 */
public final class Cells {
	/** By rank. CEILING leaves the rock as it is, as beta's rooms do, unless it would fall. */
	public static final byte NONE = 0, CEILING = 1, COBBLE = 2, MOSS = 3, AIR = 4, GRAVEL = 5, WATER = 6, WEB = 7, SPAWNER = 8, CHEST = 9;
	private static final int SIDE = Layout.REACH * 2 + 1;
	/** From a sunk room's floor to a room's ceiling. */
	private static final int BELOW = Layout.SUNK + 1, HEIGHT = BELOW + Layout.ROOM_HEIGHT + 1;

	public interface Visitor {
		void cell(int x, int y, int z, byte kind);
	}

	private final int x0, y0, z0;
	private final byte[] kinds = new byte[SIDE * SIDE * HEIGHT];

	Cells(Site site) {
		x0 = site.x - Layout.REACH;
		y0 = site.y - BELOW;
		z0 = site.z - Layout.REACH;
	}

	private int index(int x, int y, int z) {
		int ix = x - x0, iy = y - y0, iz = z - z0;
		if (ix < 0 || ix >= SIDE || iz < 0 || iz >= SIDE || iy < 0 || iy >= HEIGHT) return -1;
		return (ix * SIDE + iz) * HEIGHT + iy;
	}

	public byte get(int x, int y, int z) {
		int i = index(x, y, z);
		return i < 0 ? NONE : kinds[i];
	}

	void paint(int x, int y, int z, byte kind) {
		int i = index(x, y, z);
		if (i < 0) throw new IllegalStateException("A dungeon cell out of reach: " + x + ", " + y + ", " + z);
		if (kinds[i] < kind) kinds[i] = kind;
	}

	public void each(Visitor visitor) {
		for (int ix = 0; ix < SIDE; ix++) {
			for (int iz = 0; iz < SIDE; iz++) {
				for (int iy = 0; iy < HEIGHT; iy++) {
					byte kind = kinds[(ix * SIDE + iz) * HEIGHT + iy];
					if (kind != NONE) visitor.cell(x0 + ix, y0 + iy, z0 + iz, kind);
				}
			}
		}
	}
}
