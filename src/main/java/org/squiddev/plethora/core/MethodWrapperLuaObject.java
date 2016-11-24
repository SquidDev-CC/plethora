package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import java.util.List;

/**
 * Handles integration with a {@link ILuaObject}.
 */
public class MethodWrapperLuaObject extends MethodWrapper implements ILuaObject {
	public MethodWrapperLuaObject(List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		super(methods, contexts);
	}

	@Override
	public Object[] callMethod(ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		IUnbakedContext context = getContext(method);
		MethodResult result = doCallMethod(getMethod(method), context, args);

		return context.getExecutor().execute(result, luaContext);
	}
}
