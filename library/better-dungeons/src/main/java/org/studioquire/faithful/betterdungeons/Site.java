package org.studioquire.faithful.betterdungeons;

/** Where beta put a dungeon: its spawner's cell and its room's half widths. Two dungeons can't share a spawner, so the cell names it. */
public final class Site {
	public final int x, y, z, halfX, halfZ;

	public Site(int x, int y, int z, int halfX, int halfZ) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.halfX = halfX;
		this.halfZ = halfZ;
	}

	public long key() {
		return (long) (x + 33554432) << 33 | (long) (z + 33554432) << 7 | y;
	}

	/** Whether a cell is beta's room, its walls, floor or ceiling. */
	public boolean holds(int cx, int cy, int cz) {
		return Math.abs(cx - x) <= halfX + 1 && Math.abs(cz - z) <= halfZ + 1 && cy >= y - 1 && cy <= y + 4;
	}
}
