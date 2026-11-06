package org.studioquire.oldfaithful.core.impl.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.TranslationStorage;
import org.studioquire.oldfaithful.core.api.BoolRule;
import org.studioquire.oldfaithful.core.api.Rule;
import org.studioquire.oldfaithful.core.impl.Rules;
import org.studioquire.oldfaithful.options.settings.PageButton;
import org.studioquire.oldfaithful.options.settings.Setting;
import org.studioquire.oldfaithful.options.settings.SettingsGroupScreen;
import org.studioquire.oldfaithful.options.settings.SettingsProvider;
import org.studioquire.oldfaithful.options.settings.SettingsRegistry;

/**
 * World rules on the settings screen, each on its mod's page: this world's while one is open, otherwise what new worlds start with.
 * Every page with rules has Lock Rules while a world is open, which locks all of them, every mod's: the lock is the world's.
 */
public final class RuleSettings implements SettingsProvider {
	private static final List<Setting> ALL = new ArrayList<>();
	public static final String LOCK = "Lock Rules", LOCKED = "Rules Locked";
	public static final String LOCK_HINT = "Locks every rule of this world, all mods', for good in game. Editing the world's file with the game closed undoes it.";
	public static final String UNLOCK_HINT = "To unlock, remove " + Rules.LOCK_KEY + "=true from the world's faithful-world.properties with the game closed.";
	public static final String WORLD_LOCKED = "This world's rules are locked.", SERVER_LOCKED = "The server you're playing on has locked its rules.";
	/** The confirm screen's: beta's two lines, the second wrapped, and its two buttons. */
	public static final String CONFIRM = "Lock this world's rules?";
	public static final String CONFIRM_WARNING = "No one can change them in game after this. Only editing the world's file with the game closed unlocks them.";
	public static final String CONFIRM_YES = "Lock", CONFIRM_NO = "Cancel";
	private static final PageButton LOCK_BUTTON = new PageButton(RuleSettings::lockLabel, LOCK_HINT, RuleSettings::lockLocked, LockRulesScreen::new);

	/** Every mod's rules on one page, for Create World; null if there are none. */
	public static Screen screen(Screen parent) {
		SettingsRegistry.get();
		return ALL.isEmpty() ? null : SettingsGroupScreen.of(parent, "World Rules", RuleSettings::note, ALL);
	}

	private static String modName(String id) {
		return FabricLoader.getInstance().getModContainer(id).map((ModContainer c) -> c.getMetadata().getName()).orElse(id);
	}

	@Override
	public void register(SettingsRegistry settings) {
		TranslationStorage text = TranslationStorage.getInstance();
		Set<String> pages = new HashSet<>();
		for (Rule rule : Rules.all()) {
			String prefix = "rule." + rule.id().replace(':', '.');
			Setting.Store store = new Setting.Store() {
				@Override
				public String read() {
					return Rules.shown(rule);
				}

				@Override
				public boolean write(String value) {
					return Rules.change(rule, value);
				}
			};
			String name = text.get(prefix + ".name"), hint = text.get(prefix + ".hint");
			Setting setting;
			if (rule instanceof BoolRule) {
				setting = Setting.stored(rule.id(), name, hint, ((BoolRule) rule).defaultValue(), store);
			} else {
				String[] valuesAndWords = new String[rule.values().size() * 2];
				for (int i = 0; i < rule.values().size(); i++) {
					valuesAndWords[2 * i] = rule.values().get(i);
					valuesAndWords[2 * i + 1] = text.get(prefix + "." + rule.values().get(i));
				}
				setting = Setting.stored(rule.id(), name, hint, rule.defaultText(), store, valuesAndWords);
			}
			String mod = rule.id().substring(0, rule.id().indexOf(':'));
			settings.mod(mod, modName(mod), RuleSettings::note);
			settings.addToMod(mod, setting.lockedWhen(RuleSettings::locked));
			if (pages.add(mod)) settings.addButtonToMod(mod, LOCK_BUTTON);
			ALL.add(setting);
		}
	}

	private static String note() {
		if (Rules.onServer()) return !Rules.serverHasRules() ? "This server doesn't have these rules." : Rules.locked() ? SERVER_LOCKED : "The server you're playing on sets these.";
		if (!Rules.worldOpen()) return "New worlds start with these. A world keeps its own.";
		return Rules.locked() ? WORLD_LOCKED : "These apply to this world.";
	}

	private static String locked() {
		if (Rules.onServer()) return !Rules.serverHasRules() ? "This server doesn't have this rule." : Rules.locked() ? SERVER_LOCKED : "The server you're playing on sets this.";
		return Rules.worldOpen() && Rules.locked() ? WORLD_LOCKED : null;
	}

	/** Shown in a world of one's own, and on a server only once it's locked, since only its owner can lock it. */
	private static String lockLabel() {
		if (Rules.onServer()) return Rules.locked() ? LOCKED : null;
		if (!Rules.worldOpen()) return null;
		return Rules.locked() ? LOCKED : LOCK;
	}

	private static String lockLocked() {
		if (!Rules.locked()) return null;
		return Rules.onServer() ? SERVER_LOCKED : UNLOCK_HINT;
	}
}
