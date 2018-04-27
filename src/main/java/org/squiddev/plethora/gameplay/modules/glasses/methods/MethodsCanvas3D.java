package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup.Group3D;
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup.Origin3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.Box;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.Line3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.ObjectRoot3D;

import static dan200.computercraft.core.apis.ArgumentHelper.optBoolean;
import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.getFloat;
import static org.squiddev.plethora.api.method.ArgumentHelper.optFloat;
import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getPoint3D;
import static org.squiddev.plethora.gameplay.modules.glasses.objects.Colourable.DEFAULT_COLOUR;

public class MethodsCanvas3D {
	@BasicMethod.Inject(value = Origin3D.class, doc = "function():table -- Create a new 3D canvas centred on the current position.")
	public static MethodResult create(IUnbakedContext<Origin3D> context, Object[] args) throws LuaException {
		IContext<Origin3D> baked = context.safeBake();
		Origin3D group = baked.getTarget();
		CanvasServer canvas = baked.getContext(CanvasServer.class);

		IWorldLocation location = baked.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		if (location == null) throw new LuaException("Cannot determine a location");

		ObjectRoot3D root = new ObjectRoot3D(canvas.newObjectId(), group.id());
		root.recentre(location);

		canvas.add(root);
		return MethodResult.result(baked.makeChild(root, root.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = Group3D.class, doc = "function(x:number, y:number, z:number[, width: number, height:number, depth:number][, colour:number[, depth:boolean]]):table -- Create a new box.")
	public static MethodResult addBox(IUnbakedContext<Group3D> context, Object[] args) throws LuaException {
		double x = getFloat(args, 0);
		double y = getFloat(args, 1);
		double z = getFloat(args, 2);

		int colour;
		double width, height, depth;
		boolean depthTesting;
		if (args.length > 3) {
			width = getFloat(args, 3);
			height = getFloat(args, 4);
			depth = getFloat(args, 5);
			colour = optInt(args, 6, DEFAULT_COLOUR);
			depthTesting = optBoolean(args, 7, true);
		} else {
			width = 1;
			height = 1;
			depth = 1;
			colour = optInt(args, 3, DEFAULT_COLOUR);
			depthTesting = optBoolean(args, 4, true);
		}

		IContext<Group3D> baked = context.safeBake();
		Group3D group = baked.getTarget();
		CanvasServer canvas = baked.getContext(CanvasServer.class);

		Box box = new Box(canvas.newObjectId(), group.id());
		box.setPosition(new Vec3d(x, y, z));
		box.setSize(width, height, depth);
		box.setColour(colour);
		box.setDepthTestingEnabled(depthTesting);

		canvas.add(box);
		return MethodResult.result(baked.makeChild(box, box.reference(canvas)).getObject());
	}

	@BasicMethod.Inject(value = Group3D.class, doc = "function(start:table, end:table[, color:number][, thickness:numbner] -- Creates a new 3d line")
	public static MethodResult addLine3D(IUnbakedContext<Group3D> context, Object[] args) throws LuaException {
		Vec3d start = getPoint3D(args, 0);
		Vec3d end = getPoint3D(args, 1);
		int colour = optInt(args, 2, DEFAULT_COLOUR);
		float thickness = optFloat(args, 3, 1);

		IContext<Group3D> baked = context.safeBake();
		Group3D group = baked.getTarget();
		CanvasServer canvas = baked.getContext(CanvasServer.class);

		Line3D line = new Line3D(canvas.newObjectId(), group.id());
		line.setVertex(0, start);
		line.setVertex(1, end);
		line.setColour(colour);
		line.setScale(thickness);

		canvas.add(line);

		return MethodResult.result(baked.makeChild(line, line.reference(canvas)).getObject());
	}
}
