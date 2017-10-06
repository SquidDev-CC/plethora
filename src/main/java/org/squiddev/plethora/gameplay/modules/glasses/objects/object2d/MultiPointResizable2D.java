package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

public interface MultiPointResizable2D extends MultiPoint2D {
	int MAX_SIZE = 255;

	void removePoint(int idx);

	void addPoint(int idx, Point2D point);
}
