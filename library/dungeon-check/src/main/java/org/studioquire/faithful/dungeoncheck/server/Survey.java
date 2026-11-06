package org.studioquire.faithful.dungeoncheck.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.studioquire.faithful.betterdungeons.BetterDungeons;
import org.studioquire.faithful.betterdungeons.Cells;
import org.studioquire.faithful.betterdungeons.Furnishing;
import org.studioquire.faithful.betterdungeons.Keeper;
import org.studioquire.faithful.betterdungeons.Layout;

/** Better Dungeons' side of the dump: loaded only when the mod is there. */
final class Survey {
	/** Every chunk the square held, as the driver loaded them. */
	static final List<Chunk> LOADED = new ArrayList<>();

	private Survey() {
	}

	static int waiting(World world) {
		return Keeper.of(world).waiting();
	}

	/** One chunk of each grown dungeon, not its spawner's, put back as if a crash had lost what was saved in it: its record gone, and its cells rock. How many. */
	static int revert(World world) {
		Map<Long, Layout> layouts = new LinkedHashMap<>();
		Map<Long, int[]> chosen = new LinkedHashMap<>();
		int[] reverted = {0};
		for (Chunk chunk : LOADED) {
			for (Layout layout : BetterDungeons.grownIn(chunk)) {
				if (chunk.x == layout.site.x >> 4 && chunk.z == layout.site.z >> 4) continue;
				layouts.put(layout.site.key(), layout);
				chosen.putIfAbsent(layout.site.key(), new int[] {chunk.x, chunk.z});
			}
		}
		for (Map.Entry<Long, Layout> e : layouts.entrySet()) {
			int[] target = chosen.get(e.getKey());
			Chunk chunk = world.getChunk(target[0], target[1]);
			Furnishing.paint(world.getSeed(), e.getValue()).each((x, y, z, kind) -> {
				if (x >> 4 == target[0] && z >> 4 == target[1] && kind != Cells.CEILING) world.setBlockWithoutNotifyingNeighbors(x, y, z, 1);
			});
			BetterDungeons.kept.set(chunk, null);
			reverted[0]++;
		}
		return reverted[0];
	}

	private static void write(DataOutputStream out, int... values) throws IOException {
		for (int v : values) out.writeInt(v);
	}

	static void write(World world, int x0, int z0, int size, DataOutputStream out) throws IOException {
		Map<Long, Layout> layouts = new LinkedHashMap<>();
		for (int cx = x0; cx < x0 + size; cx++) {
			for (int cz = z0; cz < z0 + size; cz++) {
				for (Layout layout : BetterDungeons.grownIn(world.getChunk(cx, cz))) layouts.put(layout.site.key(), layout);
			}
		}
		out.writeInt(layouts.size());
		long seed = world.getSeed();
		for (Layout layout : layouts.values()) {
			write(out, layout.site.x, layout.site.y, layout.site.z, layout.site.halfX, layout.site.halfZ, layout.rooms.size(), layout.halls.size());
			for (Layout.Room room : layout.rooms) write(out, room.x0, room.z0, room.x1, room.z1, room.sink, room.depth);
			for (Layout.Hall hall : layout.halls) {
				write(out, hall.from, hall.to, hall.cells.length);
				write(out, hall.cells);
			}
			Cells cells = Furnishing.paint(seed, layout);
			List<int[]> painted = new ArrayList<>();
			cells.each((x, y, z, kind) -> painted.add(new int[] {x, y, z, kind}));
			out.writeInt(painted.size());
			for (int[] cell : painted) {
				write(out, cell[0], cell[1], cell[2]);
				out.writeByte(cell[3]);
				out.writeUTF(contents(world, cell));
			}
		}
	}

	/** What a chest or spawner really holds, as read from the world: it's what a save has to bring back. */
	private static String contents(World world, int[] cell) {
		if (cell[3] < Cells.SPAWNER) return "";
		BlockEntity entity = world.getBlockEntity(cell[0], cell[1], cell[2]);
		if (entity instanceof MobSpawnerBlockEntity) {
			// Its getter is the client's alone.
			NbtCompound nbt = new NbtCompound();
			entity.writeNbt(nbt);
			return nbt.getString("EntityId");
		}
		if (!(entity instanceof ChestBlockEntity)) return "none";
		ChestBlockEntity chest = (ChestBlockEntity) entity;
		StringBuilder text = new StringBuilder("chest");
		for (int i = 0; i < chest.size(); i++) {
			ItemStack stack = chest.getStack(i);
			if (stack != null) text.append(' ').append(i).append(':').append(stack.itemId).append('x').append(stack.count).append('/').append(stack.getDamage());
		}
		return text.toString();
	}
}
