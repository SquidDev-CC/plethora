package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.ILuaObject;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.glasses.BaseObject;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup;

import static org.squiddev.plethora.gameplay.modules.PlethoraModules.GLASSES_S;

public class MethodsCanvas {
	@PlethoraMethod(module = GLASSES_S, doc = "-- Get the 2D canvas for these glasses.", worldThread = false)
	public static ILuaObject canvas(IContext<IModuleContainer> context, @FromSubtarget(GLASSES_S) CanvasServer server) {
		return context.makeChildId(server.canvas2d()).getObject();
	}

	@PlethoraMethod(module = GLASSES_S, doc = "-- Get the 3D canvas for these glasses.", worldThread = false)
	public static ILuaObject canvas3d(IContext<IModuleContainer> context, @FromSubtarget(GLASSES_S) CanvasServer server) {
		return context.makeChildId(server.canvas3d()).getObject();
	}

	@PlethoraMethod(doc = "-- Remove all objects.", worldThread = false)
	public static void clear(@FromTarget ObjectGroup group, @FromContext CanvasServer canvas) {
		canvas.clear(group);
	}

	@PlethoraMethod(doc = "-- Remove this object from the canvas.", worldThread = false)
	public static void remove(@FromTarget BaseObject object, @FromContext CanvasServer canvas) {
		canvas.remove(object);
	}
}
