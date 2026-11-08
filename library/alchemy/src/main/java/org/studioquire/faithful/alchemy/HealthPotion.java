package org.studioquire.faithful.alchemy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.studioquire.faithful.waterbottles.Bottle;
import org.studioquire.faithful.waterbottles.WaterBottles;

public final class HealthPotion extends Bottle {
	/** A full bar, in half-hearts. */
	private static final int FULL = 20;

	private final int halfHearts;

	HealthPotion(int id, int texture, int halfHearts) {
		super(id, texture);
		this.halfHearts = halfHearts;
		maxCount = 1;
	}

	/**
	 * Heals at once where the world runs, by the plain {@code heal}, so anything over-time another mod has going carries on beside it. A full bar leaves it undrunk, as cake isn't eaten.
	 * No guess on a client: its health can be a packet behind, and a wrong guess of a drink would stay, as a server only corrects a hand that changed there. The answer comes as a slot update.
	 */
	@Override
	public ItemStack use(ItemStack stack, World world, PlayerEntity user) {
		if (world.isRemote || user.health >= FULL) return stack;
		user.heal(halfHearts);
		return exchange(stack, world, user, new ItemStack(WaterBottles.glassBottle.get()));
	}
}
