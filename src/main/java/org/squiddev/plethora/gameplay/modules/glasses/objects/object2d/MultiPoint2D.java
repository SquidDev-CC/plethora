package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.Vec2d;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint2D {
	Vec2d getPoint(int idx);

	void setVertex(int idx, Vec2d point);

	int getVertices();

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int):number, number -- Get the specified vertex of this object.")
	static MethodResult getPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Vec2d point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y);
	}

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int, x:number, y:number) -- Set the specified vertex of this object.")
	static MethodResult setPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.setVertex(idx - 1, new Vec2d(getFloat(args, 1), getFloat(args, 2)));
		return MethodResult.empty();
	}
}
