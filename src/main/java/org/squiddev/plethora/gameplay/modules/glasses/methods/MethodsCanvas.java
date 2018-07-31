package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasServer;

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
}
