package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.GlassesInstance;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;

import static org.squiddev.plethora.api.method.ArgumentHelper.*;
import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getPoint;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public class MethodsCanvas {
	@SubtargetedModuleMethod.Inject(
		target = GlassesInstance.class, module = PlethoraModules.GLASSES_S,
		doc = "function():table -- Get the canvas for these glasses."
	)
	public static MethodResult canvas(IUnbakedContext<IModuleContainer> context, Object[] args) throws LuaException {
		IContext<IModuleContainer> baked = context.safeBake();
		GlassesInstance server = baked.getContext(GlassesInstance.class);
		return MethodResult.result(baked.makeChild(server.getCanvas()).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(int:id) -- Remove this given object from the canvas.")
	public static MethodResult removeObject(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		CanvasServer canvas = context.safeBake().getTarget();

		BaseObject object = canvas.getObject(getInt(args, 0));
		if (object == null) throw new LuaException("No such object");
		canvas.remove(object);

		return MethodResult.result();
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(x:number, y:number, width:number, height:number[, color:number]):table -- Create a new rectangle.")
	public static MethodResult addRectangle(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		float x = getFloat(args, 0);
		float y = getFloat(args, 1);
		float width = getFloat(args, 2);
		float height = getFloat(args, 3);
		int colour = optInt(args, 4, DEFAULT_COLOUR);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Rectangle rectangle = new Rectangle(canvas.newObjectId());
		rectangle.setPosition(x, y);
		rectangle.setSize(width, height);
		rectangle.setColour(colour);

		canvas.add(rectangle);

		return MethodResult.result(baked.makeChild(rectangle.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(startX:number, startY:number, endX:number, endY:number[, color:number][, thickness:number]):table -- Create a new line.")
	public static MethodResult addLine(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		float startX = getFloat(args, 0);
		float startY = getFloat(args, 1);
		float endX = getFloat(args, 2);
		float endY = getFloat(args, 3);
		int colour = optInt(args, 4, DEFAULT_COLOUR);
		float thickness = optFloat(args, 5, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Line line = new Line(canvas.newObjectId());
		line.setStart(startX, startY);
		line.setEnd(endX, endY);
		line.setColour(colour);
		line.setScale(thickness);

		canvas.add(line);

		return MethodResult.result(baked.makeChild(line.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(x:number, y:number, [, color:number][, size:number]):table -- Create a new dot.")
	public static MethodResult addDot(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		float x = getFloat(args, 0);
		float y = getFloat(args, 1);
		int colour = optInt(args, 2, DEFAULT_COLOUR);
		float size = optFloat(args, 3, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Dot dot = new Dot(canvas.newObjectId());
		dot.setPosition(x, y);
		dot.setColour(colour);
		dot.setScale(size);

		canvas.add(dot);

		return MethodResult.result(baked.makeChild(dot.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(x:number, y:number, text:string, [, color:number][, size:number]):table -- Create a new text object.")
	public static MethodResult addString(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		float x = getFloat(args, 0);
		float y = getFloat(args, 1);
		String contents = getString(args, 2);
		int colour = optInt(args, 3, DEFAULT_COLOUR);
		float size = optFloat(args, 4, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Text text = new Text(canvas.newObjectId());
		text.setPosition(x, y);
		text.setText(contents);
		text.setColour(colour);
		text.setScale(size);

		canvas.add(text);

		return MethodResult.result(baked.makeChild(text.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(p1:table, p2:table, p3:table, [, color:number]):table -- Create a new triangle, composed of three points.")
	public static MethodResult addTriangle(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point2D a = getPoint(args, 0);
		Point2D b = getPoint(args, 1);
		Point2D c = getPoint(args, 2);

		int colour = optInt(args, 3, DEFAULT_COLOUR);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Triangle triangle = new Triangle(canvas.newObjectId());
		triangle.setVertex(0, a.x, a.y);
		triangle.setVertex(1, b.x, b.y);
		triangle.setVertex(2, c.x, c.y);
		triangle.setColour(colour);

		canvas.add(triangle);

		return MethodResult.result(baked.makeChild(triangle.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(points...:table, [, color:number]):table -- Create a new polygon, composed of many points.")
	public static MethodResult addPolygon(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Polygon polygon = new Polygon(canvas.newObjectId());

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i == args.length - 1 && arg instanceof Number) {
				polygon.setColour(((Number) arg).intValue());
			} else {
				Point2D point = getPoint(args, i);
				polygon.addPoint(i, point.x, point.y);
			}
		}

		canvas.add(polygon);
		return MethodResult.result(baked.makeChild(polygon.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(points...:table, [, color:number][, thickness:number]):table -- Create a new line loop, composed of many points.")
	public static MethodResult addLines(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		LineLoop lines = new LineLoop(canvas.newObjectId());

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i == args.length - 2 && arg instanceof Number) {
				lines.setColour(((Number) arg).intValue());
			} else if (i == args.length - 1 && arg instanceof Number) {
				lines.setScale(((Number) arg).floatValue());
			} else {
				Point2D point = getPoint(args, i);
				lines.addPoint(i, point.x, point.y);
			}
		}

		canvas.add(lines);
		return MethodResult.result(baked.makeChild(lines.reference(canvas)).getObject());
	}


	@BasicMethod.Inject(value = CanvasServer.class, doc = "function() -- Clear this canvas.")
	public static MethodResult clear(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake().getTarget().clear();
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function():number, number -- Get the size of this canvas.")
	public static MethodResult getSize(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake();
		return MethodResult.result(CanvasHandler.WIDTH, CanvasHandler.HEIGHT);
	}
}
