package org.studioquire.faithful.betterdungeons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.studioquire.oldfaithful.core.api.Worldgen;

/**
 * What a layout is, cell by cell: beta's walls and floors for new rooms, spawners in about half, chests mostly far from beta's room, and a few rooms and corridors with something happened to them.
 * All of it follows from the layout and the seed, so a saved layout paints the same dungeon again.
 */
public final class Furnishing {
	private static final String SPIDER = "Spider";

	private final long seed;
	private final Layout layout;
	private final Cells cells;
	private final Random random;
	private final int floor;
	/** Columns just inside a door, which stay clear. */
	private final Set<Long> fronts = new HashSet<>();

	private Furnishing(long seed, Layout layout) {
		this.seed = seed;
		this.layout = layout;
		cells = new Cells(layout.site);
		random = Mix.random(seed, layout.site.x, layout.site.y, layout.site.z, Mix.FURNISHING);
		floor = layout.site.y;
	}

	public static Cells paint(long seed, Layout layout) {
		Furnishing f = new Furnishing(seed, layout);
		for (int r = 1; r < layout.rooms.size(); r++) f.room(layout.rooms.get(r));
		for (Layout.Hall hall : layout.halls) f.hall(hall);
		f.furnish();
		return f.cells;
	}

	/** Beta's chance of moss, cell by cell. */
	private byte floorKind(int x, int y, int z) {
		return Mix.hash(seed, x, y, z, Mix.MOSS) % 4 != 0 ? Cells.MOSS : Cells.COBBLE;
	}

	/** The cell decides, so two dungeons wanting one spawner want the same one. */
	static String monster(long seed, int x, int y, int z) {
		return Worldgen.dungeonMonster(Mix.random(seed, x, y, z, Mix.MONSTER));
	}

	private void room(Layout.Room room) {
		int bottom = floor - room.sink;
		for (int x = room.x0 - 1; x <= room.x1 + 1; x++) {
			for (int z = room.z0 - 1; z <= room.z1 + 1; z++) {
				boolean wall = x < room.x0 || x > room.x1 || z < room.z0 || z > room.z1;
				cells.paint(x, bottom - 1, z, floorKind(x, bottom - 1, z));
				for (int y = bottom; y < floor + Layout.ROOM_HEIGHT; y++) cells.paint(x, y, z, wall ? Cells.COBBLE : y < floor ? Cells.WATER : Cells.AIR);
				cells.paint(x, floor + Layout.ROOM_HEIGHT, z, Cells.CEILING);
			}
		}
	}

	private boolean inRoom(int x, int y, int z) {
		for (Layout.Room room : layout.rooms) {
			if (room.shell(x, y, z, floor)) return true;
		}
		return false;
	}

	private void hall(Layout.Hall hall) {
		for (int i = 0; i < hall.cells.length; i += 2) {
			int cx = hall.cells[i], cz = hall.cells[i + 1];
			for (int x = cx - 1; x <= cx + 1; x++) {
				for (int z = cz - 1; z <= cz + 1; z++) {
					if (!inRoom(x, floor - 1, z)) cells.paint(x, floor - 1, z, floorKind(x, floor - 1, z));
					for (int y = floor; y < floor + Layout.HALL_HEIGHT; y++) {
						if (!inRoom(x, y, z)) cells.paint(x, y, z, Cells.COBBLE);
					}
					if (!inRoom(x, floor + Layout.HALL_HEIGHT, z)) cells.paint(x, floor + Layout.HALL_HEIGHT, z, Cells.CEILING);
				}
			}
		}
		for (int i = 0; i < hall.cells.length; i += 2) {
			for (int y = floor; y < floor + Layout.HALL_HEIGHT; y++) cells.paint(hall.cells[i], y, hall.cells[i + 1], Cells.AIR);
		}
		int[] out = Layout.WAYS[hall.wayOut], in = Layout.WAYS[hall.wayIn];
		for (int y = floor; y < floor + Layout.HALL_HEIGHT; y++) {
			cells.paint(hall.doorOutX(), y, hall.doorOutZ(), Cells.AIR);
			cells.paint(hall.doorInX(), y, hall.doorInZ(), Cells.AIR);
		}
		fronts.add(column(hall.doorOutX() - out[0], hall.doorOutZ() - out[1]));
		fronts.add(column(hall.doorInX() + in[0], hall.doorInZ() + in[1]));
	}

	private static long column(int x, int z) {
		return (long) x << 32 | z & 0xFFFFFFFFL;
	}

	private void furnish() {
		List<Layout.Room> rooms = new ArrayList<>(layout.rooms.subList(1, layout.rooms.size()));
		Collections.shuffle(rooms, random);

		// About half of all the rooms, beta's being one.
		int spawners = (layout.rooms.size() + 1) / 2 - 1;
		boolean webs = false;
		for (Layout.Room room : rooms) {
			if (spawners == 0 || room.sink > 0) continue;
			spawners--;
			int x = room.x0 + room.x1 >> 1, z = room.z0 + room.z1 >> 1;
			cells.paint(x, floor, z, Cells.SPAWNER);
			if (monster(seed, x, floor, z).equals(SPIDER) && random.nextInt(4) != 0) {
				webs(room);
				webs = true;
			}
		}
		if (!webs && !rooms.isEmpty() && random.nextInt(8) == 0) {
			Layout.Room room = rooms.get(0);
			if (room.sink == 0) webs(room);
		}

		int deepest = 0;
		for (Layout.Room room : rooms) deepest = Math.max(deepest, room.depth);
		boolean first = true;
		for (Layout.Room room : rooms) {
			int chests;
			if (room.depth == deepest) {
				// The far end pays: at least one chest, wherever the dungeon ends.
				chests = first ? 1 + (random.nextInt(3) == 0 ? 1 : 0) : random.nextInt(2);
				first = false;
			} else {
				chests = random.nextInt(room.depth > 1 ? 2 : 5) == 0 ? 1 : 0;
			}
			for (int i = 0; i < chests; i++) chest(room);
		}

		if (random.nextInt(3) == 0) collapse();
	}

	/** Against a wall and clear of doors and corners, as beta's stand, and not beside another: beta's two can meet, but three would be a chest with no partner. */
	private void chest(Layout.Room room) {
		int y = floor - room.sink;
		for (int tries = 0; tries < 16; tries++) {
			int x, z;
			if (random.nextBoolean()) {
				x = random.nextBoolean() ? room.x0 : room.x1;
				z = room.z0 + 1 + random.nextInt(room.z1 - room.z0 - 1);
			} else {
				z = random.nextBoolean() ? room.z0 : room.z1;
				x = room.x0 + 1 + random.nextInt(room.x1 - room.x0 - 1);
			}
			if (fronts.contains(column(x, z))) continue;
			if (cells.get(x - 1, y, z) == Cells.CHEST || cells.get(x + 1, y, z) == Cells.CHEST || cells.get(x, y, z - 1) == Cells.CHEST || cells.get(x, y, z + 1) == Cells.CHEST) continue;
			if (cells.get(x, y, z) >= Cells.SPAWNER) continue;
			cells.paint(x, y, z, Cells.CHEST);
			return;
		}
	}

	/** Thick in the top corners and along the walls, with a few lower down. */
	private void webs(Layout.Room room) {
		int count = 8 + random.nextInt(10);
		for (int i = 0; i < count; i++) {
			int x = room.x0 + random.nextInt(room.x1 - room.x0 + 1), z = room.z0 + random.nextInt(room.z1 - room.z0 + 1);
			if (random.nextBoolean()) x = random.nextBoolean() ? room.x0 : room.x1;
			if (random.nextBoolean()) z = random.nextBoolean() ? room.z0 : room.z1;
			int y = floor + Layout.ROOM_HEIGHT - 1 - (random.nextInt(4) == 0 ? 2 + random.nextInt(2) : random.nextInt(2));
			if (fronts.contains(column(x, z)) && y < floor + Layout.HALL_HEIGHT) continue;
			cells.paint(x, y, z, Cells.WEB);
		}
	}

	/** The middle of a corridor fallen in: full to the ceiling, with what spilled either side of it. */
	private void collapse() {
		List<Layout.Hall> long_ = new ArrayList<>();
		for (Layout.Hall hall : layout.halls) {
			if (hall.length() >= 3) long_.add(hall);
		}
		if (long_.isEmpty()) return;
		Layout.Hall hall = long_.get(random.nextInt(long_.size()));
		int length = hall.length(), fill = Math.min(length - 2, 2 + random.nextInt(2));
		int start = 1 + random.nextInt(length - fill - 1);
		for (int i = start - 1; i <= start + fill; i++) {
			if (i < 0 || i >= length) continue;
			int x = hall.cells[i * 2], z = hall.cells[i * 2 + 1];
			cells.paint(x, floor, z, Cells.GRAVEL);
			if (i >= start && i < start + fill) cells.paint(x, floor + 1, z, Cells.GRAVEL);
		}
	}
}
