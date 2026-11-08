package org.studioquire.faithful.alchemy.selftest.client;

import java.util.Arrays;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.util.math.MathHelper;
import org.studioquire.faithful.alchemy.Alchemy;
import org.studioquire.faithful.harness.test.client.ClientTest;
import org.studioquire.faithful.waterbottles.WaterBottles;
import org.studioquire.oldfaithful.core.api.Attachment;
import org.studioquire.oldfaithful.core.api.client.Renderers;

/** A client's real clicks at the bench and in the inventory, its real right clicks where a server has the last word, and the pictures. */
public final class AlchemyClientTest extends ClientTest {
	private static final int LIMIT = 40;
	/** The first hotbar slot in each screen: the bench's grid is nine cells and the inventory's four and four of armour. */
	private static final int BENCH_HOTBAR = 37, INVENTORY_HOTBAR = 36;

	private Item rose, weak, plain, strong, glass;
	private int x, feet, z;

	private ItemStack held() {
		return mc.player.inventory.getSelectedItem();
	}

	private boolean holding(Item item, int count) {
		return held() != null && held().getItem() == item && held().count == count;
	}

	private int count(Item item) {
		int n = 0;
		for (ItemStack s : mc.player.inventory.main) if (s != null && s.getItem() == item) n += s.count;
		return n;
	}

	private void slot(int index, int button, boolean shift) {
		mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, index, button, shift, mc.player);
	}

	/** Takes a whole stack onto the cursor, one into each cell, and whatever's left goes back. */
	private void deal(int from, int... cells) {
		slot(from, 0, false);
		for (int cell : cells) slot(cell, 1, false);
		slot(from, 0, false);
	}

	private void atBench() {
		scene("kit", "gold", 8, "real rose", 2);
		scene("bench");
		await("the gold in the hand", LIMIT, () -> held() != null && held().getItem() == Item.GOLD_INGOT);
		then(() -> mc.interactionManager.interactBlock(mc.player, mc.world, held(), x - 2, feet, z, 1));
		await("the bench to open", LIMIT, () -> mc.player.currentScreenHandler instanceof CraftingScreenHandler);
		then(() -> {
			deal(BENCH_HOTBAR, 2, 4, 6, 8, 2, 4, 6, 8);
			deal(BENCH_HOTBAR + 1, 5, 5);
		});
		wait(5);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("the bench shows a golden rose for the plus", result != null && result.getItem() == rose);
			slot(0, 0, true);
		});
		wait(10);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("a shift-click makes one, as beta's does when the result stays the same size, and shows the next", count(rose) == 1 && result != null && result.getItem() == rose);
			slot(0, 0, true);
		});
		wait(10);
		then(() -> check("and another uses up the grid, the client agreeing", count(rose) == 2 && mc.player.currentScreenHandler.getSlot(0).getStack() == null));
		scene("expect", "rose", 2);
		scene("expect", "gold", 0);
		then(() -> mc.player.closeHandledScreen());
		// Back on the inventory's screen, a server compares what's there with what that screen last sent, from before the bench: the next tick puts that right, and a kit before it would leave strays on the client.
		wait(5);

		scene("kit", "water", 1, "water", 1, "water", 1, "rose", 6);
		scene("bench");
		await("the kit", LIMIT, () -> count(rose) == 6);
		then(() -> mc.interactionManager.interactBlock(mc.player, mc.world, held(), x - 2, feet, z, 1));
		await("the bench to open", LIMIT, () -> mc.player.currentScreenHandler instanceof CraftingScreenHandler);
		then(() -> {
			deal(BENCH_HOTBAR, 9);
			deal(BENCH_HOTBAR + 3, 1, 3, 7);
		});
		wait(5);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("the bench shows a strong potion for a bottle and three roses, spread out", result != null && result.getItem() == strong);
			slot(0, 0, true);
		});
		wait(10);
		then(() -> {
			check("a shift-click takes it, with nothing left in the grid", count(strong) == 1 && count(rose) == 3);
			mc.player.closeHandledScreen();
		});
		scene("expect", "strong", 1);
		scene("expect", "rose", 3);
	}

	/** Goes on from {@link #atBench()}'s kit: two water bottles and three roses. */
	private void inInventory() {
		then(() -> {
			check("the inventory's grid is the current screen", mc.player.currentScreenHandler == mc.player.playerScreenHandler);
			deal(INVENTORY_HOTBAR + 1, 1);
			deal(INVENTORY_HOTBAR + 3, 4);
		});
		wait(5);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("the inventory's grid shows a weak potion", result != null && result.getItem() == weak);
			slot(0, 0, true);
		});
		wait(10);
		then(() -> check("a shift-click takes it", count(weak) == 1 && count(rose) == 2));
		scene("expect", "weak", 1);
		then(() -> {
			deal(INVENTORY_HOTBAR + 2, 2);
			deal(INVENTORY_HOTBAR + 3, 3, 4);
		});
		wait(5);
		then(() -> {
			ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
			check("and a plain one", result != null && result.getItem() == plain);
			slot(0, 0, false);
			slot(INVENTORY_HOTBAR + 8, 0, false);
		});
		wait(10);
		then(() -> check("by an ordinary click too", mc.player.inventory.main[8] != null && mc.player.inventory.main[8].getItem() == plain && count(rose) == 0));
		scene("expect", "plain", 1);
		scene("expect", "rose", 0);
	}

	private void drink() {
		mc.interactionManager.interactItem(mc.player, mc.world, held());
	}

	private void drinks(String potion, Item item, int to) {
		scene("kit", potion, 1);
		scene("health", 4);
		await("the potion in the hand and the wound known", LIMIT, () -> holding(item, 1) && mc.player.health == 4);
		then(() -> {
			drink();
			if (remote()) check("no guess on a client: it waits for the server's word", holding(item, 1) && mc.player.health == 4);
		});
		until(potion + ": the hand holds the bottle and the health is there", LIMIT, () -> holding(glass, 1) && mc.player.health == to);
		wait(10);
		then(() -> {
			check("and stays that way", holding(glass, 1) && mc.player.health == to);
			check("nothing else drunk", count(item) == 0);
		});
		scene("expect health", to);
	}

	private void notDrunk() {
		scene("kit", "strong", 1);
		scene("health", 20);
		await("the potion in the hand at full health", LIMIT, () -> holding(strong, 1) && mc.player.health == 20);
		ItemStack[] before = {null};
		then(() -> {
			before[0] = held();
			check("at full health the click does nothing, so no bounce of the hand", !mc.interactionManager.interactItem(mc.player, mc.world, before[0]));
		});
		for (int tick = 0; tick < 12; tick++) {
			then(() -> check("the same stack in the hand, tick after tick", held() == before[0]));
		}
		scene("expect", "strong", 1);
		scene("expect health", 20);
	}

	private void disagrees() {
		scene("kit", "weak", 1);
		scene("health", 4);
		await("the potion in the hand and the wound known", LIMIT, () -> holding(weak, 1) && mc.player.health == 4);
		then(() -> {
			mc.player.health = 20;
			drink();
			check("a client that thinks it's whole doesn't stop it", holding(weak, 1));
		});
		until("the server drinks, and says so", LIMIT, () -> holding(glass, 1) && mc.player.health == 12);

		scene("kit", "weak", 1);
		scene("health", 20);
		await("the potion in the hand at full health", LIMIT, () -> holding(weak, 1) && mc.player.health == 20);
		ItemStack[] before = {null};
		then(() -> {
			before[0] = held();
			mc.player.health = 4;
			drink();
		});
		wait(10);
		then(() -> {
			check("a client that thinks it's wounded guesses no drink the server won't make", held() == before[0]);
			mc.player.health = 20;
		});
		scene("expect", "weak", 1);
	}

	private void fromStack() {
		scene("kit", "weak", 3);
		scene("health", 4);
		await("three in the hand and the wound known", LIMIT, () -> holding(weak, 3) && mc.player.health == 4);
		then(this::drink);
		until("three in a slot: two in the hand, and the client hears of the bottle in another slot", LIMIT, () -> holding(weak, 2) && count(glass) == 1);
		scene("expect", "glass", 1);
	}

	@SuppressWarnings("unchecked")
	private static Attachment<PlayerEntity, Integer> endsAt() {
		try {
			return (Attachment<PlayerEntity, Integer>) Class.forName("org.studioquire.faithful.heartyfood.HeartyFood").getField("endsAt").get(null);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	/** What the ghost hearts draw from, as this client hears it, with Hearty Food beside: a potion moves them with the hearts. */
	private void ghostHearts() {
		Attachment<PlayerEntity, Integer> end = endsAt();
		scene("kit", "weak", 1);
		scene("eat bread", 2);
		await("ghost hearts for the bread, and the potion in the hand", LIMIT, () -> end.get(mc.player) == 10 && mc.player.health == 2 && holding(weak, 1));
		wait(1);
		then(this::drink);
		await("a potion over it: hearts at once, and the ghost hearts on past them by what's coming", LIMIT, () -> holding(glass, 1) && mc.player.health >= 10 && end.get(mc.player) == 18);
		picture("ghost-hearts-after-a-potion");
		scene("health", 20);
		await("the ghost hearts gone at a full bar", LIMIT, () -> mc.player.health == 20 && end.get(mc.player) == 0);
	}

	private static boolean sameShape(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++) if (a[i] >>> 24 != b[i] >>> 24) return false;
		return a.length == b.length;
	}

	private static boolean allGold(int[] pixels) {
		return Arrays.stream(pixels).allMatch(p -> p >>> 24 == 0 || (p >> 16 & 255) >= (p >> 8 & 255) && (p >> 8 & 255) > (p & 255));
	}

	private void gilding() {
		int[] theRose = Renderers.tilePixels(new ItemStack(Block.ROSE)), gilded = Renderers.tilePixels(new ItemStack(rose));
		check("the golden rose has the rose's shape, in gold", sameShape(theRose, gilded) && allGold(gilded) && Arrays.stream(gilded).anyMatch(p -> p >>> 24 != 0));
	}

	private void pictures() {
		scene("row");
		then(() -> {
			mc.player.inventory.selectedSlot = 8;
			mc.player.pitch = 0.0F;
		});
		wait(5);
		picture("hotbar");
		then(() -> mc.setScreen(new InventoryScreen(mc.player)));
		wait(5);
		picture("inventory");
		then(() -> mc.setScreen(null));
		for (int slot : new int[] {3, 4, 5, 6}) {
			then(() -> mc.player.inventory.selectedSlot = slot);
			wait(20);
			picture("hand-" + slot);
		}
		then(() -> mc.player.inventory.selectedSlot = 0);
	}

	@Override
	protected void script() {
		rose = Alchemy.goldenRose.get();
		weak = Alchemy.weakPotion.get();
		plain = Alchemy.healthPotion.get();
		strong = Alchemy.strongPotion.get();
		glass = WaterBottles.glassBottle.get();
		if (restarted()) {
			then(() -> check("the client has them back", count(weak) == 1 && count(plain) == 1 && count(strong) == 1 && count(rose) == 5));
			// Peaceful heals a point every 20 ticks (beta's `age % 20 * 12`): in the very tick of the wound, the client would hear of 5 and never of 4.
			scene("no peaceful");
			scene("health", 4);
			await("the wound known", LIMIT, () -> mc.player.health == 4);
			then(() -> {
				mc.player.inventory.selectedSlot = 2;
				drink();
			});
			until("and a potion still drinks", LIMIT, () -> holding(glass, 1) && mc.player.health == 20);
			scene("peaceful again");
			then(() -> mc.player.inventory.selectedSlot = 0);
			return;
		}
		then(() -> {
			x = MathHelper.floor(mc.player.x);
			feet = MathHelper.floor(mc.player.boundingBox.minY);
			z = MathHelper.floor(mc.player.z);
			mc.setScreen(null);
			TranslationStorage names = TranslationStorage.getInstance();
			check("the names", names.get(rose.getTranslationKey() + ".name").equals("Golden Rose") && names.get(weak.getTranslationKey() + ".name").equals("Weak Health Potion")
					&& names.get(plain.getTranslationKey() + ".name").equals("Health Potion") && names.get(strong.getTranslationKey() + ".name").equals("Strong Health Potion"));
			if (!remote()) gilding();
		});
		scene("no peaceful");
		atBench();
		inInventory();
		drinks("weak", weak, 12);
		drinks("plain", plain, 16);
		drinks("strong", strong, 20);
		notDrunk();
		fromStack();
		if (remote()) disagrees();
		if (FabricLoader.getInstance().isModLoaded("hearty-food")) ghostHearts();
		scene("peaceful again");
		pictures();
		scene("leave");
	}
}
