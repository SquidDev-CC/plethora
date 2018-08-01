package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import org.squiddev.plethora.utils.Vec2d;

/**
 * An object which can be positioned in 2D.
 */
public interface Positionable2D {
	Vec2d getPosition();

	void setPosition(Vec2d position);
}
