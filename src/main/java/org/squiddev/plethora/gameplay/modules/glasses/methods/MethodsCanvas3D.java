package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.math.Vec3d;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Group3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup.Origin3D;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.ObjectFrame;
import org.squiddev.plethora.gameplay.modules.glasses.objects.object3d.ObjectRoot3D;

import static org.squiddev.plethora.gameplay.modules.glasses.methods.ArgumentPointHelper.getVec3d;

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

	@BasicMethod.Inject(value = Group3D.class, doc = "function(position:table):table -- Create a new frame to put 2d objects in.")
	public static MethodResult addFrame(IUnbakedContext<Group3D> context, Object[] args) throws LuaException {
		Vec3d position = getVec3d(args, 0);

		IContext<Group3D> baked = context.safeBake();
		Group3D group = baked.getTarget();
		CanvasServer canvas = baked.getContext(CanvasServer.class);

		ObjectFrame frame = new ObjectFrame(canvas.newObjectId(), group.id());
		frame.setPosition(position);

		canvas.add(frame);
		return MethodResult.result(baked.makeChild(frame, frame.reference(canvas)).getObject());
	}
}
