package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Group3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Origin3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.*;

import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;
import static org.squiddev.plethora.api.method.ArgumentHelper.optFloat;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public final class MethodsCanvas3D {
	private MethodsCanvas3D() {
	}

	@PlethoraMethod(doc = "-- Create a new 3D canvas centred relative to the current position.", worldThread = false)
	public static TypedLuaObject<ObjectRoot3D> create(
		IContext<Origin3D> baked, @FromContext CanvasServer canvas, @FromContext(ContextKeys.ORIGIN) IWorldLocation location,
		@Optional Vec3d offset
	) {
		if (offset == null) offset = Vec3d.ZERO;

		Origin3D group = baked.getTarget();

		ObjectRoot3D root = new ObjectRoot3D(canvas.newObjectId(), group.id());
		root.recentre(location.getWorld(), location.getLoc().add(offset));

		canvas.add(root);
		return baked.makeChild(root, canvas.reference(root)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a new frame to put 2d objects in.", worldThread = false)
	public static TypedLuaObject<ObjectFrame> addFrame(IContext<Group3D> baked, @FromContext CanvasServer canvas, Vec3d position) {
		Group3D group = baked.getTarget();

		ObjectFrame frame = new ObjectFrame(canvas.newObjectId(), group.id());
		frame.setPosition(position);

		canvas.add(frame);
		return baked.makeChild(frame, canvas.reference(frame)).getObject();
	}

	@PlethoraMethod(worldThread = false,
		doc = "function(x:number, y:number, z:number[, width:number, height:number, depth:number][, color:number]):table -- Create a new box."
	)
	public static TypedLuaObject<Box> addBox(IContext<Group3D> baked, @FromContext CanvasServer canvas, Object[] args) throws LuaException {
		double x = getFloat(args, 0);
		double y = getFloat(args, 1);
		double z = getFloat(args, 2);

		int colour;
		double width, height, depth;
		if (args.length <= 4) {
			width = 1;
			height = 1;
			depth = 1;
			colour = optInt(args, 3, DEFAULT_COLOUR);
		} else {
			width = getFloat(args, 3);
			height = getFloat(args, 4);
			depth = getFloat(args, 5);
			colour = optInt(args, 6, DEFAULT_COLOUR);
		}

		Group3D group = baked.getTarget();

		Box box = new Box(canvas.newObjectId(), group.id());
		box.setPosition(new Vec3d(x, y, z));
		box.setSize(width, height, depth);
		box.setColour(colour);

		canvas.add(box);

		return baked.makeChild(box, canvas.reference(box)).getObject();
	}

	@PlethoraMethod(worldThread = false,
		doc = "-- Create a new line."
	)
	public static TypedLuaObject<Line3D> addLine(
		IContext<Group3D> baked, @FromContext CanvasServer canvas,
		Vec3d start, Vec3d end,
		@Optional(defDoub = 1.0f) float thickness, @Optional(defInt = DEFAULT_COLOUR) int colour
	) {
		Group3D group = baked.getTarget();

		Line3D line = new Line3D(canvas.newObjectId(), group.id());
		line.setPosition(start);
		line.setEndPosition(end);
		line.setScale(thickness);
		line.setColour(colour);

		canvas.add(line);

		return baked.makeChild(line, canvas.reference(line)).getObject();
	}

	@PlethoraMethod(doc = "-- Create a item model.", worldThread = false)
	public static TypedLuaObject<Item3D> addItem(
		IContext<ObjectGroup.Group3D> baked, @FromContext CanvasServer canvas,
		Vec3d position, Item item, @Optional(defInt = 0) int damage, @Optional(defDoub = 1) float scale
	) {
		ObjectGroup.Group3D group = baked.getTarget();

		Item3D model = new Item3D(canvas.newObjectId(), group.id());
		model.setPosition(position);
		model.setScale(scale);
		model.setItem(item);
		model.setDamage(damage);

		canvas.add(model);
		return baked.makeChild(model, canvas.reference(model)).getObject();
	}
}
