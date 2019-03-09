package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public interface MultiPointResizable2D extends MultiPoint2D {
	int MAX_SIZE = 255;

	void removePoint(int idx);

	void addPoint(int idx, @Nonnull Vec2d point);

	@PlethoraMethod(doc = "-- Get the number of verticies on this object.", worldThread = false)
	static int getPointCount(@FromTarget MultiPointResizable2D object) {
		return object.getVertices();
	}

	@PlethoraMethod(doc = "function(idx:int) -- Remove the specified vertex of this object.", worldThread = false)
	static void removePoint(@FromTarget MultiPointResizable2D object, int idx) throws LuaException {
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");
		object.removePoint(idx - 1);
	}

	@PlethoraMethod(doc = "function([idx:int, ]x:number, y:number) -- Add a specified vertex to this object.", worldThread = false)
	static void insertPoint(@FromTarget MultiPointResizable2D object, Object[] args) throws LuaException {
		float x, y;
		int idx;

		if (object.getVertices() > MultiPointResizable2D.MAX_SIZE) {
			throw new LuaException("To many vertices");
		}

		if (args.length >= 3) {
			idx = getInt(args, 0);
			x = getFloat(args, 1);
			y = getFloat(args, 2);

			assertBetween(idx, 1, object.getVertices() + 1, "Index out of range (%s)");
		} else {
			idx = object.getVertices();
			x = getFloat(args, 0);
			y = getFloat(args, 1);
		}

		object.addPoint(idx - 1, new Vec2d(x, y));
	}
}
