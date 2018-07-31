package org.squiddev.plethora.gameplay.modules.glasses.methods;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.glasses.GlassesInstance;

public class MethodsCanvas {
	@SubtargetedModuleMethod.Inject(
		target = GlassesInstance.class, module = PlethoraModules.GLASSES_S,
		doc = "function():table -- Get the canvas for these glasses."
	)
	public static MethodResult canvas(IUnbakedContext<IModuleContainer> context, Object[] args) throws LuaException {
		IContext<IModuleContainer> baked = context.safeBake();
		GlassesInstance server = baked.getContext(PlethoraModules.GLASSES_S, GlassesInstance.class);
		return MethodResult.result(baked.makeChild(server.getCanvas()).getObject());
	}
}
