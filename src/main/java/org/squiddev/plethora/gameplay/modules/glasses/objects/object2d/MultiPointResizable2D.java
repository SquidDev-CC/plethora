package org.squiddev.plethora.gameplay.modules.glasses.objects.object2d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.Vec2d;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public interface MultiPointResizable2D extends MultiPoint2D {
	int MAX_SIZE = 255;

	void removePoint(int idx);

	void addPoint(int idx, @Nonnull Vec2d point);

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function():int -- Get the number of verticies on this object.")
	static MethodResult getPointCount(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		return MethodResult.result(object.getVertices());
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int) -- Remove the specified vertex of this object.")
	static MethodResult removePoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.removePoint(idx - 1);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int, x:number, y:number) -- Add a specified vertex to this object.")
	static MethodResult insertPoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

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
		return MethodResult.empty();
	}
}
