package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

public interface MultiPointResizable3D extends MultiPoint3D {
	int MAX_SIZE = 255;

	void removePoint(int idx);

	void addPoint(int idx, Point3D point);
}
