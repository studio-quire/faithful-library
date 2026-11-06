package org.studioquire.faithful.harness.test.client;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import org.studioquire.faithful.harness.test.Checks;
import org.studioquire.faithful.harness.test.Script;
import org.studioquire.faithful.harness.test.TestHost;
import org.studioquire.oldfaithful.selftest.ScreenCapture;
import org.studioquire.oldfaithful.selftest.SelfTest;
import org.studioquire.oldfaithful.selftest.WorldCheck;

/**
 * A mod's test on the client, in single player and joined to a real server, fresh and after a restart: entrypoint {@code faithful:test-client}. It runs after the mod's {@code WorldTest},
 * with the player still on its plot. What the client can only ask of whoever runs the world goes through {@link #scene}.
 */
public abstract class ClientTest extends Script implements WorldCheck {
	protected Minecraft mc;

	@Override
	public final void start(Minecraft mc, SelfTest results) {
		this.mc = mc;
		begin(Checks.of(results.owner()));
	}

	@Override
	public final boolean tick(Minecraft mc) {
		return advance();
	}

	@Override
	public final boolean afterRestart() {
		return true;
	}

	/** On a real server, rather than single player. */
	protected final boolean remote() {
		return mc.world.isRemote;
	}

	protected final boolean restarted() {
		return TestHost.restarted();
	}

	protected final PlayerEntity player() {
		return mc.player;
	}

	/** A step: runs the mod's {@code WorldTest} scene of this name where the world runs, and waits for its steps to end. */
	protected final void scene(String name, Object... args) {
		int[] asked = {0};
		then(() -> {
			asked[0] = TestHost.answers + 1;
			TestHost.channel.toServer(out -> {
				out.writeByte(TestHost.OP_SCENE);
				out.writeUTF(checks().mod());
				out.writeUTF(name);
				out.writeByte(args.length);
				for (Object arg : args) out.writeUTF(String.valueOf(arg));
			});
		});
		await("the scene " + name + " to end", 3000, () -> TestHost.answers >= asked[0]);
	}

	/** A step: a picture of what's in the window now, world or screen, as {@code shots/<mod>/<name>.png} in the game's folder. */
	protected final void picture(String name) {
		ScreenCapture[] shot = {null};
		then(() -> shot[0] = ScreenCapture.request(new File("shots/" + checks().mod() + "/" + name + ".png"), -1, -1));
		await("the picture " + name, 100, () -> shot[0].done());
	}
}
