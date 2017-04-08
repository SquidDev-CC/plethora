package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.MultiPointResizable2D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.MultiPoint3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.MultiPointResizable3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.Point3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.Positionable3D;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

public class Methods3D {
	@BasicMethod.Inject(value = Positionable3D.class, doc = "function():number, number, number -- Get the position for this object.")
	public static MethodResult getPosition(IUnbakedContext<Positionable3D> context, Object[] args) throws LuaException {
		Positionable3D object = context.safeBake().getTarget();
		Point3D position = object.getPosition();
		return MethodResult.result(position.x, position.y, position.z);
	}

	@BasicMethod.Inject(value = Positionable3D.class, doc = "function(x:number, y:number, z:number) -- Set the position for this object.")
	public static MethodResult setPosition(IUnbakedContext<Positionable3D> context, Object[] args) throws LuaException {
		Positionable3D object = context.safeBake().getTarget();
		object.setPosition(new Point3D(getFloat(args, 0), getFloat(args, 1), getFloat(args, 2)));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPoint3D.class, doc = "function(idx:int):number, number, number -- Get the specified vertex of this object.")
	public static MethodResult getPoint(IUnbakedContext<MultiPoint3D> context, Object[] args) throws LuaException {
		MultiPoint3D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Point3D point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y);
	}

	@BasicMethod.Inject(value = MultiPoint3D.class, doc = "function(idx:int, x:number, y:number, z:number) -- Set the specified vertex of this object.")
	public static MethodResult setPoint(IUnbakedContext<MultiPoint3D> context, Object[] args) throws LuaException {
		MultiPoint3D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.setVertex(idx - 1, new Point3D(getFloat(args, 1), getFloat(args, 2), getFloat(args, 3)));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable3D.class, doc = "function():int -- Get the number of verticies on this object.")
	public static MethodResult getPointCount(IUnbakedContext<MultiPointResizable3D> context, Object[] args) throws LuaException {
		MultiPointResizable3D object = context.safeBake().getTarget();

		return MethodResult.result(object.getVertices());
	}

	@BasicMethod.Inject(value = MultiPointResizable3D.class, doc = "function(idx:int) -- Remove the specified vertex of this object.")
	public static MethodResult removePoint(IUnbakedContext<MultiPointResizable3D> context, Object[] args) throws LuaException {
		MultiPointResizable3D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.removePoint(idx - 1);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable3D.class, doc = "function(idx:int, x:number, y:number) -- Add a specified vertex to this object.")
	public static MethodResult insertPoint(IUnbakedContext<MultiPointResizable3D> context, Object[] args) throws LuaException {
		MultiPointResizable3D object = context.safeBake().getTarget();

		float x, y, z;
		int idx;

		if (object.getVertices() > MultiPointResizable2D.MAX_SIZE) {
			throw new LuaException("To many vertices");
		}

		if (args.length >= 3) {
			idx = getInt(args, 0);
			x = getFloat(args, 1);
			y = getFloat(args, 2);
			z = getFloat(args, 3);

			assertBetween(idx, 1, object.getVertices() + 1, "Index out of range (%s)");
		} else {
			idx = object.getVertices();
			x = getFloat(args, 0);
			y = getFloat(args, 1);
			z = getFloat(args, 2);
		}

		object.addPoint(idx - 1, new Point3D(x, y, z));
		return MethodResult.empty();
	}
}
