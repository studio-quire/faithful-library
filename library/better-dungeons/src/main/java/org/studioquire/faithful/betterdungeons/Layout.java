package org.studioquire.faithful.betterdungeons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.nbt.NbtCompound;

/**
 * Rooms and the corridors between them, on one level, a tree from beta's room. A pure function of the seed, the site and the final ground, so it's the same dungeon whatever order chunks came in.
 * Nothing here touches the world, and what doesn't fit just isn't there: one less room, never a hole.
 */
public final class Layout {
	/** Everything a dungeon writes or looks at stays this close to its spawner, which keeps it to five chunks each way. */
	public static final int REACH = 30;
	public static final int ROOM_HEIGHT = 4, HALL_HEIGHT = 2, SUNK = 2;
	private static final int ATTEMPTS = 80;
	/** Across the inside of a room: beta's are 5 and 7. */
	private static final int[] SIZES = {4, 5, 5, 6, 7, 7, 8};
	static final int[][] WAYS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

	public static final class Room {
		public final int x0, z0, x1, z1, sink, depth;

		Room(int x0, int z0, int x1, int z1, int sink, int depth) {
			this.x0 = x0;
			this.z0 = z0;
			this.x1 = x1;
			this.z1 = z1;
			this.sink = sink;
			this.depth = depth;
		}

		/** Whether a cell is the room or its walls, floor and ceiling, given the level's floor. */
		boolean shell(int x, int y, int z, int floor) {
			return x >= x0 - 1 && x <= x1 + 1 && z >= z0 - 1 && z <= z1 + 1 && y >= floor - 1 - sink && y <= floor + ROOM_HEIGHT;
		}

		/** Whether a column stands anywhere near the room: its walls or the cell beyond them. */
		boolean beside(int x, int z) {
			return x >= x0 - 2 && x <= x1 + 2 && z >= z0 - 2 && z <= z1 + 2;
		}
	}

	/** A corridor: {@code cells} are x, z pairs from just past one room's wall to just short of the other's. */
	public static final class Hall {
		public final int from, to, wayOut, wayIn;
		public final int[] cells;

		Hall(int from, int to, int wayOut, int wayIn, int[] cells) {
			this.from = from;
			this.to = to;
			this.wayOut = wayOut;
			this.wayIn = wayIn;
			this.cells = cells;
		}

		public int length() {
			return cells.length / 2;
		}

		int doorOutX() {
			return cells[0] - WAYS[wayOut][0];
		}

		int doorOutZ() {
			return cells[1] - WAYS[wayOut][1];
		}

		int doorInX() {
			return cells[cells.length - 2] + WAYS[wayIn][0];
		}

		int doorInZ() {
			return cells[cells.length - 1] + WAYS[wayIn][1];
		}
	}

	public final Site site;
	public final List<Room> rooms = new ArrayList<>();
	public final List<Hall> halls = new ArrayList<>();

	private Layout(Site site) {
		this.site = site;
		rooms.add(new Room(site.x - site.halfX, site.z - site.halfZ, site.x + site.halfX, site.z + site.halfZ, 0, 0));
	}

	static Layout plan(long seed, Site site, Ground ground) {
		Random random = Mix.random(seed, site.x, site.y, site.z, Mix.LAYOUT);
		Layout layout = new Layout(site);
		int wanted = 3 + random.nextInt(4);
		for (int i = 0; i < ATTEMPTS && layout.rooms.size() < wanted; i++) layout.grow(random, ground);
		return layout;
	}

	private void grow(Random random, Ground ground) {
		// Mostly on from the newest room, so a dungeon goes somewhere rather than sitting round beta's room like a floor plan.
		int from = random.nextInt(5) < 3 ? rooms.size() - 1 : random.nextInt(rooms.size());
		Room source = rooms.get(from);
		int way = random.nextInt(4);
		int dx = WAYS[way][0], dz = WAYS[way][1];
		int doorX = dx > 0 ? source.x1 + 1 : dx < 0 ? source.x0 - 1 : source.x0 + random.nextInt(source.x1 - source.x0 + 1);
		int doorZ = dz > 0 ? source.z1 + 1 : dz < 0 ? source.z0 - 1 : source.z0 + random.nextInt(source.z1 - source.z0 + 1);
		if (from == 0 && !betaDoor(ground, doorX, doorZ, dx, dz)) return;

		int first = 2 + random.nextInt(4), second = 0, wayIn = way;
		if (random.nextInt(4) == 0) {
			wayIn = (way < 2 ? 2 : 0) + random.nextInt(2);
			second = 2 + random.nextInt(3);
		}
		int[] cells = new int[(first + second) * 2];
		int x = doorX, z = doorZ;
		for (int i = 0; i < first + second; i++) {
			int[] step = WAYS[i < first ? way : wayIn];
			x += step[0];
			z += step[1];
			cells[i * 2] = x;
			cells[i * 2 + 1] = z;
		}

		int deep = SIZES[random.nextInt(SIZES.length)], wide = SIZES[random.nextInt(SIZES.length)], shift = random.nextInt(wide);
		boolean alongX = WAYS[wayIn][0] != 0;
		int step = alongX ? WAYS[wayIn][0] : WAYS[wayIn][1];
		// Past the corridor's end is the new room's wall, and then the room.
		int near = (alongX ? x : z) + 2 * step, far = near + (deep - 1) * step;
		int a0 = Math.min(near, far), a1 = Math.max(near, far);
		int b0 = (alongX ? z : x) - shift, b1 = b0 + wide - 1;
		boolean flood = !flooded() && random.nextInt(10) == 0;
		for (int sink = flood ? SUNK : 0; sink >= 0; sink -= SUNK) {
			Room room = alongX ? new Room(a0, b0, a1, b1, sink, source.depth + 1) : new Room(b0, a0, b1, a1, sink, source.depth + 1);
			if (fits(room, cells, from, ground)) {
				rooms.add(room);
				halls.add(new Hall(from, rooms.size() - 1, way, wayIn, cells));
				return;
			}
		}
	}

	private boolean flooded() {
		for (Room room : rooms) {
			if (room.sink > 0) return true;
		}
		return false;
	}

	/** Beta's wall stands here, both cells of a doorway, with its room clear in front: a cave, a chest or the spawner decide otherwise. */
	private boolean betaDoor(Ground ground, int x, int z, int dx, int dz) {
		for (int y = site.y; y < site.y + HALL_HEIGHT; y++) {
			if (ground.at(x, y, z) != Ground.FIRM || !ground.air(x - dx, y, z - dz)) return false;
		}
		return true;
	}

	private boolean fits(Room room, int[] cells, int from, Ground ground) {
		if (!within(room.x0 - 2, room.z0 - 2) || !within(room.x1 + 2, room.z1 + 2)) return false;
		for (Room other : rooms) {
			if (room.x0 - 1 <= other.x1 + 1 && room.x1 + 1 >= other.x0 - 1 && room.z0 - 1 <= other.z1 + 1 && room.z1 + 1 >= other.z0 - 1) return false;
		}
		for (Hall hall : halls) {
			for (int i = 0; i < hall.cells.length; i += 2) {
				if (room.beside(hall.cells[i], hall.cells[i + 1])) return false;
			}
		}
		for (int i = 0; i < cells.length; i += 2) {
			int cx = cells[i], cz = cells[i + 1];
			if (!within(cx - 2, cz - 2) || !within(cx + 2, cz + 2)) return false;
			for (int r = 0; r < rooms.size(); r++) {
				if (r != from && rooms.get(r).beside(cx, cz)) return false;
			}
			for (Hall hall : halls) {
				for (int j = 0; j < hall.cells.length; j += 2) {
					if (Math.abs(hall.cells[j] - cx) <= 2 && Math.abs(hall.cells[j + 1] - cz) <= 2) return false;
				}
			}
		}

		int floor = site.y;
		// The room: its shell ours to dig, and something firm beyond that, two deep over its head.
		for (int x = room.x0 - 2; x <= room.x1 + 2; x++) {
			for (int z = room.z0 - 2; z <= room.z1 + 2; z++) {
				for (int y = floor - 2 - room.sink; y <= floor + ROOM_HEIGHT + 2; y++) {
					int g = ground.at(x, y, z);
					if (room.shell(x, y, z, floor) ? g != Ground.DIGGABLE : g == Ground.OPEN) return false;
				}
			}
		}

		// The corridor: the same, where it isn't already one of its two rooms.
		Room source = rooms.get(from);
		for (int i = 0; i < cells.length; i += 2) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					int x = cells[i] + dx, z = cells[i + 1] + dz;
					boolean close = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
					for (int y = floor - 2; y <= floor + HALL_HEIGHT + 2; y++) {
						if (source.shell(x, y, z, floor) || room.shell(x, y, z, floor)) continue;
						int g = ground.at(x, y, z);
						if (close && y >= floor - 1 && y <= floor + HALL_HEIGHT ? g != Ground.DIGGABLE : g == Ground.OPEN) return false;
					}
				}
			}
		}
		return true;
	}

	private boolean within(int x, int z) {
		return Math.abs(x - site.x) <= REACH && Math.abs(z - site.z) <= REACH;
	}

	private Set<Long> chunks;

	/** The chunks it reaches, as {@link Keeper#chunkKey}s. */
	Set<Long> chunks() {
		if (chunks != null) return chunks;
		chunks = new TreeSet<>();
		for (int r = 1; r < rooms.size(); r++) {
			Room room = rooms.get(r);
			for (int cx = room.x0 - 1 >> 4; cx <= room.x1 + 1 >> 4; cx++) {
				for (int cz = room.z0 - 1 >> 4; cz <= room.z1 + 1 >> 4; cz++) chunks.add(Keeper.chunkKey(cx, cz));
			}
		}
		for (Hall hall : halls) {
			for (int i = 0; i < hall.cells.length; i += 2) {
				for (int cx = hall.cells[i] - 1 >> 4; cx <= hall.cells[i] + 1 >> 4; cx++) {
					for (int cz = hall.cells[i + 1] - 1 >> 4; cz <= hall.cells[i + 1] + 1 >> 4; cz++) chunks.add(Keeper.chunkKey(cx, cz));
				}
			}
		}
		return chunks;
	}

	NbtCompound write() {
		NbtCompound nbt = writeSite(site);
		byte[] roomBytes = new byte[(rooms.size() - 1) * 6];
		for (int r = 1; r < rooms.size(); r++) {
			Room room = rooms.get(r);
			int at = (r - 1) * 6;
			roomBytes[at] = (byte) (room.x0 - site.x);
			roomBytes[at + 1] = (byte) (room.z0 - site.z);
			roomBytes[at + 2] = (byte) (room.x1 - site.x);
			roomBytes[at + 3] = (byte) (room.z1 - site.z);
			roomBytes[at + 4] = (byte) room.sink;
			roomBytes[at + 5] = (byte) room.depth;
		}
		nbt.putByteArray("Rooms", roomBytes);
		int size = 0;
		for (Hall hall : halls) size += 5 + hall.cells.length;
		byte[] hallBytes = new byte[size];
		int at = 0;
		for (Hall hall : halls) {
			hallBytes[at++] = (byte) hall.from;
			hallBytes[at++] = (byte) hall.to;
			hallBytes[at++] = (byte) hall.wayOut;
			hallBytes[at++] = (byte) hall.wayIn;
			hallBytes[at++] = (byte) hall.length();
			for (int i = 0; i < hall.cells.length; i += 2) {
				hallBytes[at++] = (byte) (hall.cells[i] - site.x);
				hallBytes[at++] = (byte) (hall.cells[i + 1] - site.z);
			}
		}
		nbt.putByteArray("Halls", hallBytes);
		return nbt;
	}

	/** What a layout's fields may hold, as {@link #plan} makes them: anything else is damage, and is thrown, so the core keeps the data as it was on disk. */
	private static void check(boolean ok, String what) {
		if (!ok) throw new IllegalArgumentException("A dungeon's layout is damaged: " + what);
	}

	private static boolean near(int offset) {
		return Math.abs(offset) <= REACH;
	}

	static Layout read(NbtCompound nbt) {
		Layout layout = new Layout(readSite(nbt));
		Site site = layout.site;
		check(site.y >= 1 && site.y <= 123 && site.halfX >= 2 && site.halfX <= 3 && site.halfZ >= 2 && site.halfZ <= 3, "its site");
		byte[] rooms = nbt.getByteArray("Rooms");
		check(rooms.length % 6 == 0 && rooms.length <= 6 * 5, "its rooms' length");
		for (int at = 0; at < rooms.length; at += 6) {
			check(near(rooms[at]) && near(rooms[at + 1]) && near(rooms[at + 2]) && near(rooms[at + 3]) && rooms[at] <= rooms[at + 2] && rooms[at + 1] <= rooms[at + 3]
					&& rooms[at + 2] - rooms[at] < 8 && rooms[at + 3] - rooms[at + 1] < 8 && (rooms[at + 4] == 0 || rooms[at + 4] == SUNK) && rooms[at + 5] >= 1 && rooms[at + 5] <= 5, "a room");
			layout.rooms.add(new Room(site.x + rooms[at], site.z + rooms[at + 1], site.x + rooms[at + 2], site.z + rooms[at + 3], rooms[at + 4], rooms[at + 5]));
		}
		byte[] halls = nbt.getByteArray("Halls");
		for (int at = 0; at < halls.length; ) {
			check(at + 5 <= halls.length, "a corridor's head");
			int from = halls[at++], to = halls[at++], wayOut = halls[at++], wayIn = halls[at++], length = halls[at++];
			check(from >= 0 && to > from && to < layout.rooms.size() && wayOut >= 0 && wayOut < 4 && wayIn >= 0 && wayIn < 4 && length >= 2 && length <= 9 && at + length * 2 <= halls.length,
					"a corridor");
			int[] cells = new int[length * 2];
			for (int i = 0; i < cells.length; i += 2) {
				check(near(halls[at]) && near(halls[at + 1]), "a corridor's cell");
				cells[i] = site.x + halls[at++];
				cells[i + 1] = site.z + halls[at++];
			}
			layout.halls.add(new Hall(from, to, wayOut, wayIn, cells));
		}
		check(layout.halls.size() == layout.rooms.size() - 1, "its corridors' count");
		return layout;
	}

	static NbtCompound writeSite(Site site) {
		NbtCompound nbt = new NbtCompound();
		nbt.putInt("X", site.x);
		nbt.putInt("Y", site.y);
		nbt.putInt("Z", site.z);
		nbt.putByte("HalfX", (byte) site.halfX);
		nbt.putByte("HalfZ", (byte) site.halfZ);
		return nbt;
	}

	static Site readSite(NbtCompound nbt) {
		return new Site(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"), nbt.getByte("HalfX"), nbt.getByte("HalfZ"));
	}
}
