package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint2D {
	Point2D getPoint(int idx);

	void setVertex(int idx, Point2D point);

	int getVertices();
}
