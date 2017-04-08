package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

/**
 * An object which can be positioned in 2D.
 */
public interface Positionable3D {
	Point3D getPosition();

	void setPosition(Point3D position);
}
