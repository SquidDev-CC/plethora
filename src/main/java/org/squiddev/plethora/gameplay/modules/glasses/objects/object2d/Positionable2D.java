package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

/**
 * An object which can be positioned in 2D.
 */
public interface Positionable2D {
	float getX();

	float getY();

	void setPosition(float x, float y);
}
