package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;

public class Methods2D {
	@BasicMethod.Inject(value = Positionable2D.class, doc = "function():number, number -- Get the position for this object.")
	public static MethodResult getPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		return MethodResult.result(object.getX(), object.getY());
	}

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function(x:number, y:number) -- Set the position for this object.")
	public static MethodResult setPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		object.setPosition(getFloat(args, 0), getFloat(args, 1));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Rectangle.class, doc = "function():number, number -- Get the size of this rectangle.")
	public static MethodResult getSize(IUnbakedContext<Rectangle> context, Object[] arguments) throws LuaException {
		Rectangle rect = context.safeBake().getTarget();
		return MethodResult.result(rect.getWidth(), rect.getHeight());
	}

	@BasicMethod.Inject(value = Rectangle.class, doc = "function(width:number, height:number) -- Set the size of this rectangle.")
	public static MethodResult setSize(IUnbakedContext<Rectangle> context, Object[] arguments) throws LuaException {
		Rectangle rect = context.safeBake().getTarget();
		rect.setSize(getFloat(arguments, 0), getFloat(arguments, 1));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Line.class, doc = "function():number, number -- Get the start position of this line.")
	public static MethodResult getStartPosition(IUnbakedContext<Line> context, Object[] arguments) throws LuaException {
		Line line = context.safeBake().getTarget();
		return MethodResult.result(line.getStartX(), line.getStartY());
	}

	@BasicMethod.Inject(value = Line.class, doc = "function(x:number, y:number) -- Set the start position of this line.")
	public static MethodResult setStartPosition(IUnbakedContext<Line> context, Object[] arguments) throws LuaException {
		Line line = context.safeBake().getTarget();
		line.setStart(getFloat(arguments, 0), getFloat(arguments, 1));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = Line.class, doc = "function():number, number -- Get the end position of this line.")
	public static MethodResult getEndPosition(IUnbakedContext<Line> context, Object[] arguments) throws LuaException {
		Line line = context.safeBake().getTarget();
		return MethodResult.result(line.getEndX(), line.getEndY());
	}

	@BasicMethod.Inject(value = Line.class, doc = "function(x:number, y:number) -- Set the end position of this line.")
	public static MethodResult setEndPosition(IUnbakedContext<Line> context, Object[] arguments) throws LuaException {
		Line line = context.safeBake().getTarget();
		line.setEnd(getFloat(arguments, 0), getFloat(arguments, 1));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int):number, number -- Get the specified vertex of this object.")
	public static MethodResult getPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVerticies(), "Index out of range (%s)");

		return MethodResult.result(object.getX(idx - 1), object.getY(idx - 1));
	}

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int, x:number, y:number) -- Set the specified vertex of this object.")
	public static MethodResult setPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVerticies(), "Index out of range (%s)");

		object.setVertex(idx - 1, getFloat(args, 1), getFloat(args, 2));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function():int -- Get the number of verticies on this object.")
	public static MethodResult getPointCount(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		return MethodResult.result(object.getVerticies());
	}


	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int) -- Remove the specified vertex of this object.")
	public static MethodResult removePoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVerticies(), "Index out of range (%s)");

		object.removePoint(idx - 1);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int, x:number, y:number) -- Add a specified vertex to this object.")
	public static MethodResult insertPoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		float x, y;
		int idx;

		if (object.getVerticies() > MultiPointResizable2D.MAX_SIZE) {
			throw new LuaException("To many vertices");
		}

		if (args.length >= 3) {
			idx = getInt(args, 0);
			x = getFloat(args, 1);
			y = getFloat(args, 2);

			assertBetween(idx, 1, object.getVerticies() + 1, "Index out of range (%s)");
		} else {
			idx = object.getVerticies();
			x = getFloat(args, 0);
			y = getFloat(args, 1);
		}

		object.addPoint(idx - 1, x, y);
		return MethodResult.empty();
	}
}
