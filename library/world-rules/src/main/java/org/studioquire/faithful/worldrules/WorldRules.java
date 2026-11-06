package org.studioquire.faithful.worldrules;

import org.studioquire.oldfaithful.core.api.Beds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.studioquire.oldfaithful.core.api.BoolRule;
import org.studioquire.oldfaithful.core.api.ChoiceRule;
import org.studioquire.oldfaithful.core.api.Days;
import org.studioquire.oldfaithful.core.api.Drops;
import org.studioquire.oldfaithful.core.api.Entities;
import org.studioquire.oldfaithful.core.api.Explosions;
import org.studioquire.oldfaithful.core.api.FaithfulMod;
import org.studioquire.oldfaithful.core.api.Mod;
import org.studioquire.oldfaithful.core.api.Players;
import org.studioquire.oldfaithful.core.api.Ticks;

/** The first library mod. A rule is one line here, two in the lang file and a hook. They run where the world runs, except that a client that knows the day's length draws its sun by it. */
public final class WorldRules implements FaithfulMod {
	/** Beta's shortest wait for the weather to change, for when the rule is taken off. */
	private static final int MIN_RAIN_TIME = 12000;
	/** Later versions' odds. */
	private static final int APPLE_ODDS = 200;

	public static BoolRule mobsBreakBlocks;
	public static BoolRule blastsDestroyItems;
	public static BoolRule keepInventory;
	public static BoolRule sleeping;
	public static ChoiceRule dayLength;
	public static ChoiceRule weather;
	public static BoolRule naturalMonsters;
	public static ChoiceRule monsterLight;
	public static BoolRule appleDrops;
	public static ChoiceRule fallingBonus;

	/**
	 * With Monster Light at None: this monster is about to appear naturally in block light, from a torch, lava or the like. Sky light is left to beta's rolls, or nothing would appear outdoors at
	 * night. Not in the Nether, where lava lights most of what there is.
	 */
	public static boolean lightStops(net.minecraft.entity.LivingEntity entity) {
		if (!monsterLight.get().equals("none") || !(entity instanceof net.minecraft.entity.mob.MonsterEntity) || entity.world.dimension.isNether) return false;
		int x = net.minecraft.util.math.MathHelper.floor(entity.x), y = net.minecraft.util.math.MathHelper.floor(entity.boundingBox.minY), z = net.minecraft.util.math.MathHelper.floor(entity.z);
		return entity.world.getBrightness(net.minecraft.world.LightType.BLOCK, x, y, z) > 0;
	}

	@Override
	public void init(Mod mod) {
		mobsBreakBlocks = mod.rules().bool("mobs_break_blocks", true);
		blastsDestroyItems = mod.rules().bool("blasts_destroy_items", true);
		keepInventory = mod.rules().bool("keep_inventory", false);
		sleeping = mod.rules().bool("sleeping", true);
		dayLength = mod.rules().choice("day_length", "normal", "double", "triple");
		weather = mod.rules().choice("weather", "normal", "clear", "rain");
		naturalMonsters = mod.rules().bool("natural_monsters", true);
		monsterLight = mod.rules().choice("monster_light", "beta", "none");
		appleDrops = mod.rules().bool("apple_drops", false);
		fallingBonus = mod.rules().choice("falling_bonus", "beta", "fixed", "off");

		Explosions.onBlocks(e -> {
			if (e.cause().isMob() && !mobsBreakBlocks.get()) e.spareBlocks();
		});
		Players.keepInventory(p -> keepInventory.get());
		Beds.onSleep(s -> {
			if (!sleeping.get()) s.setSpawnOnly();
		});
		Days.length(length -> {
			switch (dayLength.get()) {
				case "double": return length * 2;
				case "triple": return length * 3;
				default: return length;
			}
		});
		Ticks.onWorld(WorldRules::holdWeather);
		Entities.onMonsterSpawning(m -> {
			if (!naturalMonsters.get()) m.cancel();
		});
		Drops.block(WorldRules::apple);
	}

	/** Oak's meta is 0 in its low bits. Leaves taken with shears reach the filter as a drop of their own, whose meta is the emptied cell's, so the stack is what tells them. */
	private static void apple(Drops.BlockDrops drops) {
		if (!appleDrops.get() || drops.block != Block.LEAVES || (drops.meta & 3) != 0) return;
		for (ItemStack stack : drops.stacks) {
			if (stack.itemId == Block.LEAVES.id) return;
		}
		if (drops.world.random.nextInt(APPLE_ODDS) == 0) drops.stacks.add(new ItemStack(Item.APPLE));
	}

	/**
	 * After the world's tick, so after a night's sleep has cleared the weather. Rain is beta's flag and its timer kept from running out: the sky, snow and what a client hears all follow from it.
	 * Thunder has its own cycle, and only shows while it rains, so it needs nothing: none under a clear sky, its usual comings and goings under a wet one.
	 */
	private static void holdWeather(World world) {
		String held = weather.get();
		if (held.equals("normal")) return;
		WorldProperties properties = world.getProperties();
		properties.setRaining(held.equals("rain"));
		if (properties.getRainTime() < 2) properties.setRainTime(MIN_RAIN_TIME);
	}
}
