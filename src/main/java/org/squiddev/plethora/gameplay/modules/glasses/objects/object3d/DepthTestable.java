package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

public interface DepthTestable {
	boolean isDepthTestingEnabled();

	void setDepthTestingEnabled(boolean testingEnabled);

	@BasicMethod.Inject(value = DepthTestable.class, doc = "function():boolean -- Get whether or not depth testing is enabled for this object")
	static MethodResult hasDepthTesting(IUnbakedContext<DepthTestable> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().isDepthTestingEnabled());
	}
}
