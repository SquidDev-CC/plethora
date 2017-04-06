package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint3D {
	Point3D getPoint(int idx);

	void setVertex(int idx, Point3D point);

	int getVertices();
}
