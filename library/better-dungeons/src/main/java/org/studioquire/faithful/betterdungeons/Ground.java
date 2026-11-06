package org.studioquire.faithful.betterdungeons;

/** The world as a layout reads it: final cells only, and with other grown dungeons not in it. They read as what they replaced, so it can't matter who was built first. */
interface Ground {
	/** Ours to dig: rock and earth. */
	int DIGGABLE = 0;
	/** Keeps things out but isn't ours to dig: beta's walls, bedrock, whatever someone built. */
	int FIRM = 1;
	/** Air, liquid, anything a sealed room can't stand beside. */
	int OPEN = 2;

	int at(int x, int y, int z);

	boolean air(int x, int y, int z);
}
