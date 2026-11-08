package org.studioquire.faithful.alchemy.selftest;

import java.util.Arrays;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipeManager;
import net.minecraft.screen.ScreenHandler;
import org.studioquire.faithful.alchemy.Alchemy;
import org.studioquire.faithful.harness.test.WorldTest;
import org.studioquire.faithful.waterbottles.WaterBottles;
import org.studioquire.oldfaithful.core.api.Attachment;

/** The recipes and the sums where the world runs, each potion through its own {@code use}. Real clicks and the pictures are {@code AlchemyClientTest}. */
public final class AlchemyTest extends WorldTest {
	private static final Item GOLD = Item.GOLD_INGOT;

	private Item rose, weak, plain, strong, water, glass;
	private int difficultyBefore;

	private ItemStack crafted(int side, Object... cells) {
		CraftingInventory grid = new CraftingInventory(new ScreenHandler() {
			@Override
			public boolean canUse(PlayerEntity player) {
				return true;
			}
		}, side, side);
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] instanceof Item) grid.setStack(i, new ItemStack((Item) cells[i]));
			else if (cells[i] instanceof Block) grid.setStack(i, new ItemStack((Block) cells[i]));
		}
		return CraftingRecipeManager.getInstance().craft(grid);
	}

	private boolean makes(Item result, ItemStack made) {
		return made != null && made.getItem() == result && made.count == 1;
	}

	private void recipes() {
		check("a golden rose: four ingots in a plus around a rose", makes(rose, crafted(3, null, GOLD, null, GOLD, Block.ROSE, GOLD, null, GOLD, null)));
		check("not round a dandelion", crafted(3, null, GOLD, null, GOLD, Block.DANDELION, GOLD, null, GOLD, null) == null);
		check("not short of an ingot", crafted(3, null, GOLD, null, GOLD, Block.ROSE, null, null, GOLD, null) == null);
		check("the clock is still the clock", makes(Item.CLOCK, crafted(3, null, GOLD, null, GOLD, Item.REDSTONE, GOLD, null, GOLD, null)));
		check("the plus doesn't fit the inventory's grid", crafted(2, GOLD, GOLD, Block.ROSE, GOLD) == null);

		check("a water bottle and a rose, at the bench, is a weak potion", makes(weak, crafted(3, water, rose)));
		check("and in any cells", makes(weak, crafted(3, null, null, rose, null, null, null, water)));
		check("two roses, the plain one", makes(plain, crafted(3, rose, null, water, null, null, null, null, null, rose)));
		check("three roses, the strong one", makes(strong, crafted(3, rose, rose, rose, water)));
		check("in the inventory's grid: weak", makes(weak, crafted(2, null, rose, water)));
		check("plain", makes(plain, crafted(2, rose, water, rose)));
		check("strong", makes(strong, crafted(2, rose, rose, water, rose)));
		check("four roses make nothing", crafted(3, water, rose, rose, rose, rose) == null);
		check("nor two bottles to a rose", crafted(3, water, water, rose) == null);
		check("nor an empty bottle", crafted(2, glass, rose) == null);
		check("nor a rose ungilded", crafted(2, water, Block.ROSE) == null);
		check("nor a bottle with nothing", crafted(2, water) == null);
		check("nor roses with no bottle", crafted(2, rose, rose) == null);
	}

	private void clearInventory() {
		Arrays.fill(player.inventory.main, null);
		player.currentScreenHandler.sendContentUpdates();
	}

	/** What the game does with a right click that no block took. */
	private void click() {
		ItemStack held = player.inventory.getSelectedItem();
		ItemStack after = held.use(world, player);
		player.inventory.main[player.inventory.selectedSlot] = after.count == 0 ? null : after;
	}

	private boolean holding(Item item, int count) {
		ItemStack held = player.inventory.getSelectedItem();
		return held != null && held.getItem() == item && held.count == count;
	}

	private int count(Item item) {
		int n = 0;
		for (ItemStack s : player.inventory.main) if (s != null && s.getItem() == item) n += s.count;
		return n;
	}

	private void drink(Item potion, int from, int to) {
		clearInventory();
		hold(new ItemStack(potion));
		player.health = from;
		click();
		check(potion.getTranslationKey() + " from " + from + " half-hearts comes to " + to + " at once, leaving the bottle: " + player.health, player.health == to && holding(glass, 1));
	}

	private void drinks() {
		check("a potion is one to a slot, a golden rose stacks", weak.getMaxCount() == 1 && plain.getMaxCount() == 1 && strong.getMaxCount() == 1 && rose.getMaxCount() == 64);
		drink(weak, 2, 10);
		drink(plain, 2, 14);
		drink(strong, 2, 18);
		drink(strong, 10, 20);
		drink(weak, 19, 20);

		clearInventory();
		ItemStack potion = new ItemStack(strong);
		hold(potion);
		player.health = 20;
		click();
		check("at full health it isn't drunk", player.inventory.getSelectedItem() == potion && holding(strong, 1) && player.health == 20);

		// What a give command can make; nothing in play gets here.
		clearInventory();
		hold(new ItemStack(weak, 3));
		player.health = 4;
		click();
		check("three in one slot: one is drunk and one bottle comes back", holding(weak, 2) && count(glass) == 1 && player.health == 12);
		player.health = 20;
		clearInventory();
	}

	@SuppressWarnings("unchecked")
	private static Attachment<PlayerEntity, Integer> hearty(String field) throws ReflectiveOperationException {
		return (Attachment<PlayerEntity, Integer>) Class.forName("org.studioquire.faithful.heartyfood.HeartyFood").getField(field).get(null);
	}

	private void eat(Item food) {
		hold(new ItemStack(food));
		click();
	}

	/**
	 * Beside Hearty Food, which this mod doesn't depend on, so by reflection, on whichever side of the merge: a potion is at once and doesn't go through its healing, and the
	 * ghost hearts stay where health and what's pending really end up. The timed part needs no Peaceful, whose own healing counts in it.
	 */
	private void besideHeartyFood() throws ReflectiveOperationException {
		Attachment<PlayerEntity, Integer> pending = hearty("pending"), endsAt = hearty("endsAt");
		then(() -> {
			if (!singlePlayer()) world.difficulty = 1;
			clearInventory();
			player.health = 2;
			eat(Item.BREAD);
			check("bread: nothing at once, eight half-hearts coming", player.health == 2 && pending.get(player) == 8 && endsAt.get(player) == 10);
			hold(new ItemStack(weak));
			click();
			check("a potion over it is at once, by its own eight, and takes nothing from what's coming: " + player.health + ", " + pending.get(player), player.health == 10 && pending.get(player) == 8);
		});
		until("and the end point rises by that: ghost hearts at 18", 3, () -> endsAt.get(player) == 18);
		then(() -> {
			hold(new ItemStack(strong));
			click();
			check("a strong one fills the bar", player.health == 20);
		});
		until("and nothing's left of the food, so no ghost hearts", 3, () -> !pending.has(player) && !endsAt.has(player));
		then(() -> {
			player.health = 18;
			eat(Item.BREAD);
			check("two half-hearts short, with a full bar on its way", pending.get(player) == 2 && endsAt.get(player) == 20);
			hold(new ItemStack(weak));
			click();
			check("isn't a full bar: a potion still drinks", player.health == 20 && holding(glass, 1));
		});
		until("and what was coming goes", 3, () -> !pending.has(player) && !endsAt.has(player));
		then(() -> {
			if (!singlePlayer()) world.difficulty = difficultyBefore;
			clearInventory();
		});
	}

	private void leaveForRestart() {
		clearInventory();
		player.health = 20;
		player.inventory.main[0] = new ItemStack(weak);
		player.inventory.main[1] = new ItemStack(plain);
		player.inventory.main[2] = new ItemStack(strong);
		player.inventory.main[3] = new ItemStack(rose, 5);
		player.currentScreenHandler.sendContentUpdates();
	}

	private ItemStack named(String what, int count) {
		switch (what) {
			case "weak": return new ItemStack(weak, count);
			case "plain": return new ItemStack(plain, count);
			case "strong": return new ItemStack(strong, count);
			case "water": return new ItemStack(water, count);
			case "rose": return new ItemStack(rose, count);
			case "gold": return new ItemStack(GOLD, count);
			case "real rose": return new ItemStack(Block.ROSE, count);
			default: throw new IllegalArgumentException(what);
		}
	}

	private void scenes() {
		// The test server is Peaceful, which heals by itself; the client's tests are timed. In single player the options set it each tick, and aren't Peaceful.
		scene("no peaceful", a -> {
			if (singlePlayer()) return;
			difficultyBefore = world.difficulty;
			world.difficulty = 1;
		});
		scene("peaceful again", a -> {
			if (!singlePlayer()) world.difficulty = difficultyBefore;
		});
		// Into the hotbar's slots from the first, the rest of the inventory emptied: name, count, name, count...
		scene("kit", a -> {
			clearInventory();
			for (int i = 0; i * 2 < a.length; i++) player.inventory.main[i] = named(a[i * 2], Integer.parseInt(a[i * 2 + 1]));
			player.inventory.selectedSlot = 0;
			player.currentScreenHandler.sendContentUpdates();
		});
		scene("health", a -> {
			player.health = Integer.parseInt(a[0]);
			player.hearts = 0;
		});
		scene("bench", a -> set(-2, 0, 0, Block.CRAFTING_TABLE));
		scene("expect", a -> {
			Item item = a[0].equals("glass") ? glass : named(a[0], 1).getItem();
			check("where the world runs: " + a[1] + " of " + a[0] + " (" + count(item) + ")", count(item) == Integer.parseInt(a[1]));
		});
		scene("expect health", a -> check("where the world runs: health " + a[0] + " (" + player.health + ")", player.health == Integer.parseInt(a[0])));
		scene("eat bread", a -> {
			player.health = Integer.parseInt(a[0]);
			player.hearts = 0;
			ItemStack inHand = player.inventory.getSelectedItem();
			eat(Item.BREAD);
			hold(inHand);
		});
		scene("row", a -> {
			clearInventory();
			player.health = 20;
			ItemStack[] row = {new ItemStack(glass), new ItemStack(water), new ItemStack(Block.ROSE), new ItemStack(rose), new ItemStack(weak), new ItemStack(plain), new ItemStack(strong),
					new ItemStack(GOLD), new ItemStack(Block.DANDELION)};
			for (int i = 0; i < row.length; i++) {
				player.inventory.main[i] = row[i];
				player.inventory.main[9 + i] = row[row.length - 1 - i].copy();
			}
			player.currentScreenHandler.sendContentUpdates();
			world.setTime(6000);
		});
		scene("leave", a -> leaveForRestart());
	}

	@Override
	protected void script() {
		rose = Alchemy.goldenRose.get();
		weak = Alchemy.weakPotion.get();
		plain = Alchemy.healthPotion.get();
		strong = Alchemy.strongPotion.get();
		water = WaterBottles.waterBottle.get();
		glass = WaterBottles.glassBottle.get();
		scenes();
		if (restarted()) {
			check("the potions and the golden roses came through the restart by name", player.inventory.main[0] != null && player.inventory.main[0].getItem() == weak
					&& player.inventory.main[1] != null && player.inventory.main[1].getItem() == plain
					&& player.inventory.main[2] != null && player.inventory.main[2].getItem() == strong
					&& player.inventory.main[3] != null && player.inventory.main[3].getItem() == rose && player.inventory.main[3].count == 5);
			return;
		}
		recipes();
		drinks();
		if (FabricLoader.getInstance().isModLoaded("hearty-food")) {
			try {
				besideHeartyFood();
			} catch (ReflectiveOperationException e) {
				check("Hearty Food has what this test reads: " + e, false);
			}
		}
		then(this::leaveForRestart);
	}
}
