package org.squiddev.plethora.gameplay.modules.glasses.objects;

/**
 * An object which can be coloured.
 */
public interface Colourable {
	int DEFAULT_COLOUR = 0xFFFFFFFF;

	int getColour();

	void setColour(int colour);
}
