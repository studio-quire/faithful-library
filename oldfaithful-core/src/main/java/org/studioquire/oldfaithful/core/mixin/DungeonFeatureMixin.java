package org.studioquire.oldfaithful.core.mixin;

import java.util.Random;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DungeonFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.studioquire.oldfaithful.core.api.Worldgen;
import org.studioquire.oldfaithful.core.impl.DungeonFeatureInvoker;

/** A dungeon placed, heard at the one line only a placed dungeon reaches, and its two lists for mods to draw from. The two ints after the height are its half widths. */
@Mixin(DungeonFeature.class)
abstract class DungeonFeatureMixin implements DungeonFeatureInvoker {
	@Shadow
	private ItemStack getRandomChestItem(Random random) {
		throw new AssertionError();
	}

	@Shadow
	private String getRandomEntity(Random random) {
		throw new AssertionError();
	}

	@Override
	public ItemStack faithful$chestItem(Random random) {
		return getRandomChestItem(random);
	}

	@Override
	public String faithful$monster(Random random) {
		return getRandomEntity(random);
	}

	@Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/MobSpawnerBlockEntity;setSpawnedEntityId(Ljava/lang/String;)V", shift = At.Shift.AFTER))
	private void faithful$placed(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 4) int halfX, @Local(ordinal = 5) int halfZ) {
		Worldgen.dungeonPlaced(world, x, y, z, halfX, halfZ);
	}
}
