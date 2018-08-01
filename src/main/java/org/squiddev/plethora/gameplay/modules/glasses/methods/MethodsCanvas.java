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
import org.squiddev.plethora.gameplay.modules.glasses.ObjectGroup;

public class MethodsCanvas {
	@SubtargetedModuleMethod.Inject(
		target = CanvasServer.class, module = PlethoraModules.GLASSES_S,
		doc = "function():table -- Get the 2D canvas for these glasses."
	)
	public static MethodResult canvas(IUnbakedContext<IModuleContainer> context, Object[] args) throws LuaException {
		IContext<IModuleContainer> baked = context.safeBake();
		CanvasServer server = baked.getContext(PlethoraModules.GLASSES_S, CanvasServer.class);
		return MethodResult.result(baked.makeChildId(server.canvas2d()).getObject());
	}

	@BasicMethod.Inject(value = ObjectGroup.class, doc = "function() -- Remove all objects.")
	public static MethodResult clear(IUnbakedContext<ObjectGroup> context, Object[] args) throws LuaException {
		IContext<ObjectGroup> baked = context.safeBake();
		baked.getContext(CanvasServer.class).clear(baked.getTarget());
		return MethodResult.empty();
	}

	@BasicMethod.Inject(value = BaseObject.class, doc = "function() -- Remove this object from the canvas.")
	public static MethodResult remove(IUnbakedContext<BaseObject> context, Object[] args) throws LuaException {
		IContext<BaseObject> baked = context.safeBake();
		baked.getContext(CanvasServer.class).remove(baked.getTarget());
		return MethodResult.result();
	}
}
