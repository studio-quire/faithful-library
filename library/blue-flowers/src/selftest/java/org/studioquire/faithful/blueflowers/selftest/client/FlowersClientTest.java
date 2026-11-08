package org.studioquire.faithful.blueflowers.selftest.client;

import net.minecraft.block.Block;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.studioquire.faithful.blueflowers.BlueFlowers;
import org.studioquire.faithful.blueflowers.selftest.FlowersTest;
import org.studioquire.faithful.harness.test.client.ClientTest;

/** A client's real right click and left click, its name, the row of three for looking at, and in single player a patch as it stands. */
public final class FlowersClientTest extends ClientTest {
	private static final int LIMIT = 60;

	private int x, feet, z;

	private ItemStack held() {
		return mc.player.inventory.getSelectedItem();
	}

	private int carried(Item item) {
		int n = 0;
		for (ItemStack s : mc.player.inventory.main) if (s != null && s.getItem() == item) n += s.count;
		return n;
	}

	private void place(int dx, int slot) {
		then(() -> {
			mc.player.inventory.selectedSlot = slot;
			mc.interactionManager.interactBlock(mc.player, mc.world, held(), x + dx, feet - 1, z + 2, 1);
		});
	}

	/** A short diagonal walk, in steps small enough that a server takes them as walking. */
	private void walk(double step) {
		for (int i = 0; i < 8; i++) then(() -> mc.player.setPosition(mc.player.x + step, mc.player.y, mc.player.z + step));
	}

	@Override
	protected void script() {
		if (restarted()) return;
		x = (int) Math.floor(mc.player.x);
		feet = (int) Math.floor(mc.player.boundingBox.minY + 0.01);
		z = (int) Math.floor(mc.player.z);
		Item flowerItem = Item.ITEMS[BlueFlowers.flower.get().id];
		int id = BlueFlowers.flower.get().id;
		then(() -> check("it's called Blue Flower", "Blue Flower".equals(TranslationStorage.getInstance().get(flowerItem.getTranslationKey() + ".name"))));
		scene("grass row");
		scene("kit");
		await("the kit in the hand", LIMIT, () -> carried(flowerItem) == 4);
		then(() -> {
			mc.player.inventory.selectedSlot = 2;
			mc.interactionManager.interactBlock(mc.player, mc.world, held(), x + 1, feet - 1, z + 1, 1);
		});
		await("the flower to stand on the client", LIMIT, () -> mc.world.getBlockId(x + 1, feet, z + 1) == id);
		wait(10);
		scene("standing");
		then(() -> mc.interactionManager.attackBlock(x + 1, feet, z + 1, 1));
		await("it to break on one hit", LIMIT, () -> mc.world.getBlockId(x + 1, feet, z + 1) == 0);
		wait(10);
		scene("broken");
		walk(0.1);
		until("the player to pick it up", 200, () -> carried(flowerItem) == 4);
		wait(10);
		scene("carrying");
		walk(-0.1);
		wait(10);

		place(-1, 0);
		place(0, 1);
		place(1, 2);
		await("all three to stand", LIMIT, () -> mc.world.getBlockId(x + 1, feet, z + 2) == id && mc.world.getBlockId(x, feet, z + 2) != 0 && mc.world.getBlockId(x - 1, feet, z + 2) != 0);
		then(() -> {
			mc.player.yaw = 0;
			mc.player.pitch = 24;
		});
		wait(20);
		picture("row");
		scene("clear");
		if (remote()) return;
		then(() -> {
			if (FlowersTest.views.isEmpty()) note("no clear view of a patch, so no pictures of one");
			mc.world.setTime((mc.world.getTime() / 24000 + 1) * 24000 + 6000);
			mc.world.getProperties().setRaining(false);
		});
		// The views are known once the world test has looked at the land, so this step lays out their steps then.
		then(() -> {
			for (FlowersTest.View view : FlowersTest.views) {
				then(() -> {
					mc.options.hideHud = true;
					mc.player.setPosition(view.x, view.y, view.z);
					mc.player.yaw = view.yaw;
					mc.player.pitch = view.pitch;
				});
				wait(240);
				picture("patch-" + view.name);
			}
		});
		then(() -> {
			mc.options.hideHud = false;
			mc.player.setPosition(x + 0.5, feet + 1.62, z + 0.5);
		});
		wait(20);
	}
}
