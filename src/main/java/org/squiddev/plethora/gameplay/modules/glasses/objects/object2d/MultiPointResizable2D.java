package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import org.squiddev.plethora.utils.Vec2d;

public interface MultiPointResizable2D extends MultiPoint2D {
	int MAX_SIZE = 255;

	void removePoint(int idx);

	void addPoint(int idx, Vec2d point);
}
