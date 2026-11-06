package org.studioquire.oldfaithful.core.api;

import java.util.List;

/** What every world rule has: {@link BoolRule} and {@link ChoiceRule}. */
public interface Rule {
	String id();

	/** The values it can hold, as its world's file and {@code /rule} write them. */
	List<String> values();

	/** Which of {@link #values} is beta's behaviour. */
	String defaultText();
}
