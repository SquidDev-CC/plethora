package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object2d.*;
import org.squiddev.plethora.utils.Vec2d;

import static dan200.computercraft.core.apis.ArgumentHelper.*;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;

public class Methods2D {
	@BasicMethod.Inject(value = Positionable2D.class, doc = "function():number, number -- Get the position for this object.")
	public static MethodResult getPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		Vec2d pos = object.getPosition();
		return MethodResult.result(pos.x, pos.y);
	}

	@BasicMethod.Inject(value = Positionable2D.class, doc = "function(x:number, y:number) -- Set the position for this object.")
	public static MethodResult setPosition(IUnbakedContext<Positionable2D> context, Object[] args) throws LuaException {
		Positionable2D object = context.safeBake().getTarget();
		object.setPosition(new Vec2d(getFloat(args, 0), getFloat(args, 1)));
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

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int):number, number -- Get the specified vertex of this object.")
	public static MethodResult getPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		Vec2d point = object.getPoint(idx - 1);
		return MethodResult.result(point.x, point.y);
	}

	@BasicMethod.Inject(value = MultiPoint2D.class, doc = "function(idx:int, x:number, y:number) -- Set the specified vertex of this object.")
	public static MethodResult setPoint(IUnbakedContext<MultiPoint2D> context, Object[] args) throws LuaException {
		MultiPoint2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.setVertex(idx - 1, new Vec2d(getFloat(args, 1), getFloat(args, 2)));
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function():int -- Get the number of verticies on this object.")
	public static MethodResult getPointCount(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		return MethodResult.result(object.getVertices());
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int) -- Remove the specified vertex of this object.")
	public static MethodResult removePoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
		MultiPointResizable2D object = context.safeBake().getTarget();

		int idx = getInt(args, 0);
		assertBetween(idx, 1, object.getVertices(), "Index out of range (%s)");

		object.removePoint(idx - 1);
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = MultiPointResizable2D.class, doc = "function(idx:int, x:number, y:number) -- Add a specified vertex to this object.")
	public static MethodResult insertPoint(IUnbakedContext<MultiPointResizable2D> context, Object[] args) throws LuaException {
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

	@BasicMethod.Inject(value = Item2D.class, doc = "function(): string, number -- Get the item and damage value for this object.")
	public static MethodResult getItem(IUnbakedContext<Item2D> context, Object[] args) throws LuaException {
		Item2D object = context.safeBake().getTarget();

		return MethodResult.result(object.getItem().getRegistryName().toString(), object.getDamage());
	}

	@BasicMethod.Inject(value = Item2D.class, doc = "function(item:string[, damage:number]) -- Set the item and damage value for this object.")
	public static MethodResult setItem(IUnbakedContext<Item2D> context, Object[] args) throws LuaException {
		Item2D object = context.safeBake().getTarget();

		ResourceLocation name = new ResourceLocation(getString(args, 0));
		int damage = optInt(args, 1, 0);

		Item item = Item.REGISTRY.getObject(name);
		if (item == null) throw new LuaException("Unknown item '" + name + "'");

		object.setItem(item);
		object.setDamage(damage);

		return MethodResult.empty();
	}
}
