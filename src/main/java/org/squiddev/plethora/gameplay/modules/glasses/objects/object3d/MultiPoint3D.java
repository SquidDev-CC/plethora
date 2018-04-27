package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

/**
 * A polygon for which you can set multiple points.
 */
public interface MultiPoint3D {
	Vec3d getPoint(int idx);

	void setVertex(int idx, Vec3d point);

	int getVertices();

	@BasicMethod.Inject(value = MultiPoint3D.class, doc = "function(idx:int):number, number, number -- Get the specified vertex of this object.")
	static MethodResult getPoint(IUnbakedContext<MultiPoint3D> context, Object[] args) throws LuaException {
		MultiPoint3D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Vec3d point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y, point.z);
	}

	@BasicMethod.Inject(value = MultiPoint3D.class, doc = "function(idx:int, x:number, y:number, z:number) -- Set the specified vertex of this object.")
	static MethodResult setPoint(IUnbakedContext<MultiPoint3D> context, Object[] args) throws LuaException {
		MultiPoint3D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.setVertex(idx - 1, new Vec3d(getFloat(args, 1), getFloat(args, 2), getFloat(args, 3)));
		return MethodResult.empty();
	}
}
