package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.*;

import static org.squiddev.plethora.api.method.ArgumentHelper.optFloat;
import static org.squiddev.plethora.api.method.ArgumentHelper.optInt;
import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getPoint3D;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public class MethodsCanvas3D {
	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(start:table, end:table[, color:number][, thickness:number]):table -- Create a new line in 3D space.")
	public static MethodResult addLine3D(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point3D start = getPoint3D(args, 0);
		Point3D end = getPoint3D(args, 1);
		int colour = optInt(args, 2, DEFAULT_COLOUR);
		float thickness = optFloat(args, 3, 1);

		IContext<CanvasServer> baked = context.safeBake();
		IWorldLocation location = baked.getContext(IWorldLocation.class);
		if (location != null) {
			start.offset(location.get());
			end.offset(location.get());
		}

		CanvasServer canvas = baked.getTarget();
		Line3D line = new Line3D(canvas.newObjectId());
		line.setVertex(0, start);
		line.setVertex(1, end);
		line.setColour(colour);
		line.setScale(thickness);

		canvas.add(line);

		return MethodResult.result(baked.makeChild(line.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(position:table, [, color:number][, size:number]):table -- Create a new dot in 3D space.")
	public static MethodResult addDot3D(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point3D position = getPoint3D(args, 0);
		int colour = optInt(args, 1, DEFAULT_COLOUR);
		float size = optFloat(args, 2, 1);

		IContext<CanvasServer> baked = context.safeBake();
		IWorldLocation location = baked.getContext(IWorldLocation.class);
		if (location != null) position.offset(location.get());

		CanvasServer canvas = baked.getTarget();
		Dot3D dot = new Dot3D(canvas.newObjectId());
		dot.setPosition(position);
		dot.setColour(colour);
		dot.setScale(size);

		canvas.add(dot);

		return MethodResult.result(baked.makeChild(dot.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(p1:table, p2:table, p3:table, [, color:number]):table -- Create a new 3D triangle, composed of three points.")
	public static MethodResult addTriangle3D(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point3D a = getPoint3D(args, 0);
		Point3D b = getPoint3D(args, 1);
		Point3D c = getPoint3D(args, 2);

		int colour = optInt(args, 3, DEFAULT_COLOUR);

		IContext<CanvasServer> baked = context.safeBake();
		IWorldLocation location = baked.getContext(IWorldLocation.class);
		if (location != null) {
			a.offset(location.get());
			b.offset(location.get());
			c.offset(location.get());
		}
		CanvasServer canvas = baked.getTarget();
		Triangle3D triangle = new Triangle3D(canvas.newObjectId());
		triangle.setVertex(0, a);
		triangle.setVertex(1, b);
		triangle.setVertex(2, c);
		triangle.setColour(colour);

		canvas.add(triangle);

		return MethodResult.result(baked.makeChild(triangle.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(points...:table, [, color:number]):table -- Create a new polygon, composed of many points.")
	public static MethodResult addPolygon(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Polygon3D polygon = new Polygon3D(canvas.newObjectId());

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i == args.length - 1 && arg instanceof Number) {
				polygon.setColour(((Number) arg).intValue());
			} else {
				polygon.addPoint(i, getPoint3D(args, i));
			}
		}

		canvas.add(polygon);
		return MethodResult.result(baked.makeChild(polygon.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(points...:table, [, color:number][, thickness:number]):table -- Create a new line loop, composed of many points.")
	public static MethodResult addLines(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		LineLoop3D lines = new LineLoop3D(canvas.newObjectId());

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i == args.length - 2 && arg instanceof Number) {
				lines.setColour(((Number) arg).intValue());
			} else if (i == args.length - 1 && arg instanceof Number) {
				lines.setScale(((Number) arg).floatValue());
			} else {
				lines.addPoint(i, getPoint3D(args, i));
			}
		}

		canvas.add(lines);
		return MethodResult.result(baked.makeChild(lines.reference(canvas)).getObject());
	}
}
