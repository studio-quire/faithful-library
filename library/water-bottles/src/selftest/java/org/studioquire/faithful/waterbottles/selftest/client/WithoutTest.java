package org.studioquire.faithful.waterbottles.selftest.client;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.util.math.MathHelper;
import org.studioquire.faithful.harness.test.client.ClientTest;
import org.studioquire.faithful.waterbottles.WaterBottles;

/**
 * A client with the mod on a server without it: the client's recipe shows bottles at the bench, the server takes them back, and no bottle is left behind.
 * On a real server's own terms: a bench and glass from {@code /give}, by real clicks.
 */
public final class WithoutTest extends ClientTest {
	private static final int LIMIT = 100;

	private int benchX, benchY, benchZ;

	private int slotOf(int id) {
		for (int i = 0; i < 9; i++) {
			ItemStack s = mc.player.inventory.main[i];
			if (s != null && s.itemId == id) return i;
		}
		return -1;
	}

	private int bottles() {
		int n = 0;
		int glass = WaterBottles.glassBottle.get().id, water = WaterBottles.waterBottle.get().id;
		for (ItemStack s : mc.player.inventory.main) if (s != null && (s.itemId == glass || s.itemId == water)) n += s.count;
		ItemStack cursor = mc.player.inventory.getCursorStack();
		return cursor != null && (cursor.itemId == glass || cursor.itemId == water) ? n + cursor.count : n;
	}

	/** Wherever the server spawned the player: bare ground two cells off, with air on it. */
	private boolean findGround() {
		int feet = MathHelper.floor(mc.player.boundingBox.minY);
		for (int[] off : new int[][] {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {2, 2}, {-2, 2}, {2, -2}, {-2, -2}}) {
			benchX = MathHelper.floor(mc.player.x) + off[0];
			benchZ = MathHelper.floor(mc.player.z) + off[1];
			int y = feet + 2;
			while (y > feet - 3 && mc.world.getBlockId(benchX, y, benchZ) == 0) y--;
			if (mc.world.getMaterial(benchX, y, benchZ).isSolid()) {
				benchY = y + 1;
				return true;
			}
		}
		return false;
	}

	@Override
	protected void script() {
		then(() -> {
			check("the client is admitted", mc.world != null && remote());
			// A gift is thrown the way the player looks: at their feet, it's picked up.
			mc.player.pitch = 90.0F;
		});
		wait(5);
		then(() -> {
			mc.player.sendChatMessage("/give " + mc.player.name + " " + Block.GLASS.id + " 3");
			mc.player.sendChatMessage("/give " + mc.player.name + " " + Block.CRAFTING_TABLE.id + " 1");
		});
		await("the server's gifts", LIMIT, () -> slotOf(Block.GLASS.id) >= 0 && slotOf(Block.CRAFTING_TABLE.id) >= 0);
		then(() -> {
			check("there's ground for a bench", findGround());
			mc.player.inventory.selectedSlot = slotOf(Block.CRAFTING_TABLE.id);
		});
		wait(3);
		then(() -> mc.interactionManager.interactBlock(mc.player, mc.world, mc.player.inventory.getSelectedItem(), benchX, benchY - 1, benchZ, 1));
		await("the bench to stand", LIMIT, () -> mc.world.getBlockId(benchX, benchY, benchZ) == Block.CRAFTING_TABLE.id);
		then(() -> mc.player.inventory.selectedSlot = slotOf(Block.GLASS.id));
		wait(3);
		then(() -> mc.interactionManager.interactBlock(mc.player, mc.world, mc.player.inventory.getSelectedItem(), benchX, benchY, benchZ, 1));
		await("the bench to open", LIMIT, () -> mc.player.currentScreenHandler instanceof CraftingScreenHandler);
		then(() -> {
			int sync = mc.player.currentScreenHandler.syncId;
			mc.interactionManager.clickSlot(sync, 37 + slotOf(Block.GLASS.id), 0, false, mc.player);
			for (int cell : new int[] {1, 3, 5}) mc.interactionManager.clickSlot(sync, cell, 1, false, mc.player);
		});
		wait(10);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("the client's own recipe shows bottles", result != null && result.getItem() == WaterBottles.glassBottle.get());
			mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, false, mc.player);
			check("and lets them be taken, as a guess", bottles() == 3);
		});
		until("the server takes them back", LIMIT, () -> bottles() == 0);
		then(() -> mc.player.closeHandledScreen());
		wait(40);
		then(() -> {
			check("no bottle is left on the client", bottles() == 0);
			check("and it's still in the game", mc.world != null && mc.player != null && mc.player.health > 0);
		});
	}
}
