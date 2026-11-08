package org.studioquire.faithful.alchemy;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.studioquire.faithful.waterbottles.WaterBottles;
import org.studioquire.oldfaithful.core.api.Content;
import org.studioquire.oldfaithful.core.api.FaithfulMod;
import org.studioquire.oldfaithful.core.api.ItemEntry;
import org.studioquire.oldfaithful.core.api.Mod;
import org.studioquire.oldfaithful.core.api.Recipes;

/** Gild a thing, then brew it. Gold is the limit: a potion's price is its roses' ingots. Another golden thing is a line in each of {@code content} and {@code init}, with its name, and a PNG. */
public final class Alchemy implements FaithfulMod {
	public static ItemEntry<Item> goldenRose;
	public static ItemEntry<HealthPotion> weakPotion, healthPotion, strongPotion;

	@Override
	public void content(Content c) {
		goldenRose = gilded(c, "golden_rose", Block.ROSE);
		weakPotion = potion(c, "weak_health_potion", 8);
		healthPotion = potion(c, "health_potion", 12);
		strongPotion = potion(c, "strong_health_potion", 16);
	}

	/** What a vanilla reader sees is the thing before it was gilded: nothing it didn't have. */
	private static ItemEntry<Item> gilded(Content c, String name, Block before) {
		return c.item(name, s -> new Item(s.id()) {}.setTextureId(s.texture(name))).standIn(Item.ITEMS[before.id]);
	}

	/** And a potion is its empty vessel, as Water Bottles' are. */
	private static ItemEntry<HealthPotion> potion(Content c, String name, int halfHearts) {
		return c.item(name, s -> new HealthPotion(s.id(), s.texture(name), halfHearts)).standIn(Item.BOWL);
	}

	@Override
	public void init(Mod mod) {
		gild(goldenRose.get(), Block.ROSE);
		brew(weakPotion.get(), 1);
		brew(healthPotion.get(), 2);
		brew(strongPotion.get(), 3);
	}

	/** Four gold ingots in a plus around {@code thing}, the clock's shape, which only fits at the bench. */
	private static void gild(Item golden, Object thing) {
		Recipes.shaped(new ItemStack(golden), " G ", "GTG", " G ", 'G', Item.GOLD_INGOT, 'T', thing);
	}

	/** A water bottle and so many golden roses, anywhere they fit: at most three, so the inventory's grid too. */
	private static void brew(Item potion, int roses) {
		Object[] ingredients = new Object[roses + 1];
		Arrays.fill(ingredients, goldenRose.get());
		ingredients[0] = WaterBottles.waterBottle.get();
		Recipes.shapeless(new ItemStack(potion), ingredients);
	}
}
