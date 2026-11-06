package org.studioquire.faithful.betterdungeons.selftest.client;

import net.minecraft.block.Block;
import org.studioquire.faithful.betterdungeons.selftest.GrowthTest;
import org.studioquire.faithful.harness.test.client.ClientTest;

/** What a client has of a dungeon that grew while it was connected: the same cells, by vanilla's own packets, and in single player a look round. */
public final class GrowthClientTest extends ClientTest {
	@Override
	protected void script() {
		int px = (int) Math.floor(mc.player.x), pz = (int) Math.floor(mc.player.z);
		int[] site = GrowthTest.site(mc.world.getSeed(), px, pz);
		wait(20);
		then(() -> {
			int walls = 0, chests = 0;
			for (int x = site[0] - 30; x <= site[0] + 30; x++) {
				for (int z = site[2] - 30; z <= site[2] + 30; z++) {
					if (Math.abs(x - site[0]) <= 4 && Math.abs(z - site[2]) <= 4) continue;
					for (int y = site[1] - 3; y <= site[1] + 3; y++) {
						int id = mc.world.getBlockId(x, y, z);
						if (id == Block.COBBLESTONE.id || id == Block.MOSSY_COBBLESTONE.id) walls++;
						if (id == Block.CHEST.id) chests++;
					}
				}
			}
			check("the client has the rooms beyond beta's: " + walls + " cells of wall and floor, " + chests + " chests", walls > 100 && chests > 0);
		});
		if (restarted() || remote()) return;
		scene("light");
		scene("visit", site[0] + 0.5, site[1] + 2.62, site[2] + 1.5);
		wait(40);
		picture("beta-room");
		scene("visit", px + 0.5, mc.player.y, pz + 0.5);
	}
}
