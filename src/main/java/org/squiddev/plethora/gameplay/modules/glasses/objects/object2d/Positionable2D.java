package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

/**
 * An object which can be positioned in 2D.
 */
public interface Positionable2D {
	@Nonnull
	Vec2d getPosition();

	void setPosition(@Nonnull Vec2d position);

	@PlethoraMethod(doc = "function():number, number -- Get the position for this object.", worldThread = false)
	static MethodResult getPosition(@FromTarget Positionable2D object) {
		Vec2d pos = object.getPosition();
		return MethodResult.result(pos.x, pos.y);
	}

	@PlethoraMethod(doc = "-- Set the position for this object.", worldThread = false)
	static void setPosition(@FromTarget Positionable2D object, double x, double y) {
		object.setPosition(new Vec2d(x, y));
	}
}
