package org.studioquire.faithful.worldrules.selftest.client;

import org.studioquire.faithful.harness.test.client.ClientTest;
import org.studioquire.faithful.worldrules.WorldRules;
import org.studioquire.oldfaithful.core.api.Days;
import org.studioquire.oldfaithful.core.impl.Rules;

/** What a client with the rule shows of it: the server's value, and a sun that moves on smoothly at its pace, between ticks too; after the restart, the lock. */
public final class RulesClientTest extends ClientTest {
	/** The server's corrections may move it a clock tick or so; one of those is 1/24000 of a day, a little over that where the sun's curve is steepest. */
	private static final float MOST_BACK = 3.0F / Days.NORMAL;
	/** A tick's milliseconds: 50, or fewer in a harness run with faster game time ({@code -Ptest.speed}). */
	private static final double TICK_MS = Integer.getInteger("oldfaithful.test.tickMs", 50);

	private void sun(String what, int divisor, int n) {
		float[] last = {-1.0F}, back = {0.0F};
		long[] from = new long[1], began = new long[1];
		int[] seen = {0};
		await(what, n + 5, () -> {
			if (seen[0] == 0) {
				from[0] = Days.time(mc.world);
				began[0] = System.currentTimeMillis();
			}
			for (float delta = 0.0F; delta < 1.0F; delta += 0.25F) {
				float sun = mc.world.getTime(delta);
				if (last[0] >= 0.0F) back[0] = Math.max(back[0], last[0] - sun);
				last[0] = sun;
			}
			return ++seen[0] >= n;
		});
		then(() -> {
			long moved = Days.time(mc.world) - from[0], ms = System.currentTimeMillis() - began[0];
			// A client on a server hears the sun by the server's ticks, so if a long frame cost it ticks of its own, the clock follows the time gone by and not its count.
			boolean paced = Math.abs(moved - (double) n / divisor) <= 3 || Math.abs(moved - ms / TICK_MS / divisor) <= 3;
			check(what + ": in " + n + " ticks (" + ms + " ms) the sun's clock moved " + moved + ", and never back by more than " + back[0] * Days.NORMAL + " of a day's 24000", paced && back[0] <= MOST_BACK);
		});
	}

	@Override
	protected void script() {
		if (restarted()) {
			wait(5);
			then(() -> check("back to normal: the client hears that too", WorldRules.dayLength.get().equals("normal") && Days.length() == Days.NORMAL));
			scene("lock");
			wait(10);
			then(() -> check("locked where the world runs: the client hears it", Rules.locked()));
			scene("unlock");
			wait(10);
			then(() -> check("and unlocked by the world's file: that too", !Rules.locked()));
			return;
		}
		wait(5);
		then(() -> check("the client has the day's length and the weather: " + Days.length(), Days.length() == 2 * Days.NORMAL && mc.world.isRaining()));
		sun("double", 2, 100);
		picture("rain");
		scene("rule", "day_length", "triple");
		wait(25);
		sun("triple", 3, 100);
		scene("rule", "day_length", "double");
	}
}
