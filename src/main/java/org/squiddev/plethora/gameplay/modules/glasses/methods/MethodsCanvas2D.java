package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;
import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;
import static org.squiddev.plethora.api.method.ArgumentHelper.optFloat;
import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getPoint2D;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public class MethodsCanvas2D {
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
		rectangle.setPosition(new Point2D(x, y));
		rectangle.setSize(width, height);
		rectangle.setColour(colour);

		canvas.add(rectangle);

		return MethodResult.result(baked.makeChild(rectangle, rectangle.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(start:table, end:table[, color:number][, thickness:number]):table -- Create a new line.")
	public static MethodResult addLine(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point2D start = getPoint2D(args, 0);
		Point2D end = getPoint2D(args, 1);
		int colour = optInt(args, 2, DEFAULT_COLOUR);
		float thickness = optFloat(args, 3, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Line line = new Line(canvas.newObjectId());
		line.setVertex(0, start);
		line.setVertex(1, end);
		line.setColour(colour);
		line.setScale(thickness);

		canvas.add(line);

		return MethodResult.result(baked.makeChild(line, line.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(position:table, [, color:number][, size:number]):table -- Create a new dot.")
	public static MethodResult addDot(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point2D position = getPoint2D(args, 0);
		int colour = optInt(args, 1, DEFAULT_COLOUR);
		float size = optFloat(args, 2, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Dot dot = new Dot(canvas.newObjectId());
		dot.setPosition(position);
		dot.setColour(colour);
		dot.setScale(size);

		canvas.add(dot);

		return MethodResult.result(baked.makeChild(dot, dot.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(position:table, text:string, [, color:number][, size:number]):table -- Create a new text object.")
	public static MethodResult addText(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point2D point = getPoint2D(args, 0);
		String contents = getString(args, 1);
		int colour = optInt(args, 2, DEFAULT_COLOUR);
		float size = optFloat(args, 3, 1);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Text text = new Text(canvas.newObjectId());
		text.setPosition(point);
		text.setText(contents);
		text.setColour(colour);
		text.setScale(size);

		canvas.add(text);

		return MethodResult.result(baked.makeChild(text, text.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(p1:table, p2:table, p3:table, [, color:number]):table -- Create a new triangle, composed of three points.")
	public static MethodResult addTriangle(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		Point2D a = getPoint2D(args, 0);
		Point2D b = getPoint2D(args, 1);
		Point2D c = getPoint2D(args, 2);

		int colour = optInt(args, 3, DEFAULT_COLOUR);

		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();
		Triangle triangle = new Triangle(canvas.newObjectId());
		triangle.setVertex(0, a);
		triangle.setVertex(1, b);
		triangle.setVertex(2, c);
		triangle.setColour(colour);

		canvas.add(triangle);

		return MethodResult.result(baked.makeChild(triangle, triangle.reference(canvas)).getObject());
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
				polygon.addPoint(i, getPoint2D(args, i));
			}
		}

		canvas.add(polygon);
		return MethodResult.result(baked.makeChild(polygon, polygon.reference(canvas)).getObject());
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
				lines.addPoint(i, getPoint2D(args, i));
			}
		}

		canvas.add(lines);
		return MethodResult.result(baked.makeChild(lines, lines.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function(position:table, id:string[, damage:number][, scale:number]):table -- Create a item icon.")
	public static MethodResult addItem(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		IContext<CanvasServer> baked = context.safeBake();
		CanvasServer canvas = baked.getTarget();

		Point2D position = getPoint2D(args, 0);
		ResourceLocation name = new ResourceLocation(getString(args, 1));
		int damage = optInt(args, 2, 0);
		float scale = optFloat(args, 3, 1);

		Item item = Item.REGISTRY.getObject(name);
		if (item == null) throw new LuaException("Unknown item '" + name + "'");

		Item2D model = new Item2D(canvas.newObjectId());
		model.setPosition(position);
		model.setScale(scale);
		model.setItem(item);
		model.setDamage(damage);

		canvas.add(model);
		return MethodResult.result(baked.makeChild(model, model.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function():number, number -- Get the size of this canvas.")
	public static MethodResult getSize(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake();
		return MethodResult.result(CanvasHandler.WIDTH, CanvasHandler.HEIGHT);
	}
}
