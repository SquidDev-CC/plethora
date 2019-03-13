package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Frame2D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Group2D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;
import org.squiddev.plethora.utils.Vec2d;

import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.optFloat;
import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getVec2d;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public class MethodsCanvas2D {
	@PlethoraMethod(doc = "-- Create a new rectangle.", worldThread = false)
	public static ILuaObject addRectangle(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		float x, float y, float width, float height, @Optional(defInt = DEFAULT_COLOUR) int colour
	) {
		Group2D group = baked.getTarget();

		Rectangle rectangle = new Rectangle(canvas.newObjectId(), group.id());
		rectangle.setPosition(new Vec2d(x, y));
		rectangle.setSize(width, height);
		rectangle.setColour(colour);

		canvas.add(rectangle);

		return baked.makeChild(rectangle, rectangle.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a new line.", worldThread = false)
	public static ILuaObject addLine(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		Vec2d start, Vec2d end, @Optional(defInt = DEFAULT_COLOUR) int colour, @Optional(defDoub = 1) float thickness
	) {
		Group2D group = baked.getTarget();

		Line line = new Line(canvas.newObjectId(), group.id());
		line.setVertex(0, start);
		line.setVertex(1, end);
		line.setColour(colour);
		line.setScale(thickness);

		canvas.add(line);

		return baked.makeChild(line, line.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "function(position:table, [, color:number][, size:number]):table -- Create a new dot.", worldThread = false)
	public static ILuaObject addDot(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		Vec2d position, @Optional(defInt = DEFAULT_COLOUR) int colour, @Optional(defDoub = 1) float size
	) {
		Group2D group = baked.getTarget();

		Dot dot = new Dot(canvas.newObjectId(), group.id());
		dot.setPosition(position);
		dot.setColour(colour);
		dot.setScale(size);

		canvas.add(dot);
		return baked.makeChild(dot, dot.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a new text object.", worldThread = false)
	public static ILuaObject addText(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		Vec2d position, String contents, @Optional(defInt = DEFAULT_COLOUR) int colour, @Optional(defDoub = 1) float size
	) {
		Group2D group = baked.getTarget();

		Text text = new Text(canvas.newObjectId(), group.id());
		text.setPosition(position);
		text.setText(contents);
		text.setColour(colour);
		text.setScale(size);

		canvas.add(text);

		return baked.makeChild(text, text.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a new triangle, composed of three points.", worldThread = false)
	public static ILuaObject addTriangle(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		Vec2d p1, Vec2d p2, Vec2d p3, @Optional(defInt = DEFAULT_COLOUR) int colour
	) {
		Group2D group = baked.getTarget();

		Triangle triangle = new Triangle(canvas.newObjectId(), group.id());
		triangle.setVertex(0, p1);
		triangle.setVertex(1, p2);
		triangle.setVertex(2, p3);
		triangle.setColour(colour);

		canvas.add(triangle);

		return baked.makeChild(triangle, triangle.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "function(points...:table, [, color:number]):table -- Create a new polygon, composed of many points.", worldThread = false)
	public static ILuaObject addPolygon(IContext<Group2D> baked, @FromContext CanvasServer canvas, Object[] args) throws LuaException {
		Group2D group = baked.getTarget();

		Polygon polygon = new Polygon(canvas.newObjectId(), group.id());
		int i;
		for (i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i >= args.length - 1 && arg instanceof Number) {
				break;
			} else {
				polygon.addPoint(i, getVec2d(args, i));
			}
		}

		polygon.setColour(optInt(args, i, DEFAULT_COLOUR));

		canvas.add(polygon);
		return baked.makeChild(polygon, polygon.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "function(points...:table, [, color:number[, thickness:number]]):table -- Create a new line loop, composed of many points.", worldThread = false)
	public static MethodResult addLines(IContext<Group2D> baked, @FromContext CanvasServer canvas, Object[] args) throws LuaException {
		Group2D group = baked.getTarget();

		LineLoop lines = new LineLoop(canvas.newObjectId(), group.id());
		int i;
		for (i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (i >= args.length - 2 && arg instanceof Number) {
				break;
			} else {
				lines.addPoint(i, getVec2d(args, i));
			}
		}

		lines.setColour(optInt(args, i, DEFAULT_COLOUR));
		lines.setScale(optFloat(args, i + 1, 1));

		canvas.add(lines);
		return MethodResult.result(baked.makeChild(lines, lines.reference(canvas)).getObject());
	}

	@PlethoraMethod(doc = "-- Create a item icon.", worldThread = false)
	public static ILuaObject addItem(
		IContext<Group2D> baked, @FromContext CanvasServer canvas,
		Vec2d position, Item item, @Optional(defInt = 0) int damage, @Optional(defDoub = 1) float scale
	) {
		Group2D group = baked.getTarget();

		Item2D model = new Item2D(canvas.newObjectId(), group.id());
		model.setPosition(position);
		model.setScale(scale);
		model.setItem(item);
		model.setDamage(damage);

		canvas.add(model);
		return baked.makeChild(model, model.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a new object group.", worldThread = false)
	public static ILuaObject addGroup(IContext<Group2D> baked, @FromContext CanvasServer canvas, Vec2d position) throws LuaException {
		Group2D group = baked.getTarget();

		ObjectGroup2D newGroup = new ObjectGroup2D(canvas.newObjectId(), group.id());
		newGroup.setPosition(position);

		canvas.add(newGroup);
		return baked.makeChild(newGroup, newGroup.reference(canvas)).getObject();
	}

	@PlethoraMethod(doc = "function():number, number -- Get the size of this canvas.", worldThread = false)
	public static MethodResult getSize(@FromTarget Frame2D target) {
		return MethodResult.result(target.getWidth(), target.getHeight());
	}
}
