package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint2D {
	@Nonnull
	Vec2d getPoint(int idx);

	void setVertex(int idx, @Nonnull Vec2d point);

	int getVertices();

	@PlethoraMethod(doc = "function(idx:int):number, number -- Get the specified vertex of this object.", worldThread = false)
	static MethodResult getPoint(@FromTarget MultiPoint2D object, int idx) throws LuaException {
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Vec2d point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y);
	}

	@PlethoraMethod(doc = "-- Set the specified vertex of this object.", worldThread = false)
	static void setPoint(@FromTarget MultiPoint2D object, int idx, double x, double y) throws LuaException {
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");
		object.setVertex(idx - 1, new Vec2d(x, y));
	}
}
