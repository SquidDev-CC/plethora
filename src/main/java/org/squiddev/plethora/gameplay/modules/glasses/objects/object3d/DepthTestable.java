package org.squiddev.plethora.gameplay.modules.glasses.objects.object3d;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import static dan200.computercraft.core.apis.ArgumentHelper.getBoolean;

public interface DepthTestable {
	boolean hasDepthTest();

	void setDepthTest(boolean depthTest);

	@BasicMethod.Inject(value = DepthTestable.class, doc = "function():boolean -- Determine whether depth testing is enabled for this object")
	static MethodResult isDepthTested(IUnbakedContext<DepthTestable> context, Object[] args) throws LuaException {
		return MethodResult.result(context.safeBake().getTarget().hasDepthTest());
	}

	@BasicMethod.Inject(value = DepthTestable.class, doc = "function(depthTest:boolean) -- Set whether depth testing is enabled for this object")
	static MethodResult setDepthTested(IUnbakedContext<DepthTestable> context, Object[] args) throws LuaException {
		boolean depth = getBoolean(args, 0);
		context.safeBake().getTarget().setDepthTest(depth);
		return MethodResult.empty();
	}
}
