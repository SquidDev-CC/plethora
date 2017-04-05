package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint2D {
	float getX(int idx);

	float getY(int idx);

	void setVertex(int idx, float x, float y);

	int getVerticies();
}
