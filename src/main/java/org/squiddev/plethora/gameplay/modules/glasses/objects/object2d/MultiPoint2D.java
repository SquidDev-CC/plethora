package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import org.squiddev.plethora.utils.Vec2d;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint2D {
	Vec2d getPoint(int idx);

	void setVertex(int idx, Vec2d point);

	int getVertices();
}
