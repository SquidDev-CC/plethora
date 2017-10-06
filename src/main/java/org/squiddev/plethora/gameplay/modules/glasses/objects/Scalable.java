package org.squiddev.plethora.gameplay.modules.glasses.objects;

/**
 * An object which can be scaled. This includes point side, text size and line thickness.
 */
public interface Scalable {
	float getScale();

	void setScale(float scale);
}
