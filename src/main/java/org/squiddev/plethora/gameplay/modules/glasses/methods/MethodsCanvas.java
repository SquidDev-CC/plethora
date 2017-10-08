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
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;
import org.squiddev.plethora.gameplay.modules.glasses.GlassesInstance;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

public class MethodsCanvas {

	@BasicMethod.Inject(value = CanvasServer.class, doc = "function() -- Clear this canvas.")
	public static MethodResult clear(IUnbakedContext<CanvasServer> context, Object[] args) throws LuaException {
		context.safeBake().getTarget().clear();
		return MethodResult.empty();
	}

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
}
