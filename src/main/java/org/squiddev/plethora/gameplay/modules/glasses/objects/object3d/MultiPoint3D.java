package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

/**
 * A three-dimensional polygon for which you can set multiple vertices.
 */
public interface MultiPoint3D {
	@Nonnull
	Vec3d getPoint(int idx);

	void setVertex(int idx, @Nonnull Vec3d point);

	int getVertices();

	@PlethoraMethod(doc = "function(idx:int):number, number, number -- Get the specified vertex of this object.", worldThread = false)
	static MethodResult getPoint(@FromTarget MultiPoint3D object, int idx) throws LuaException {
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Vec3d point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y, point.z);
	}

	@PlethoraMethod(doc = "-- Set the specified vertex of this object.", worldThread = false)
	static void setPoint(@FromTarget MultiPoint3D object, int idx, double x, double y, double z) throws LuaException {
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");
		object.setVertex(idx - 1, new Vec3d(x, y, z));
	}
}
