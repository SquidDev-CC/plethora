package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import java.util.List;

/**
 * Handles integration with a {@link ILuaObject}.
 */
public class MethodWrapperLuaObject extends MethodWrapper implements ILuaObject {
	private final IComputerAccess access;

	public MethodWrapperLuaObject(List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts, IComputerAccess access) {
		super(methods, contexts);
		this.access = access;
	}

	@Override
	public Object[] callMethod(ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		IUnbakedContext context = getContext(method).withContext(getReferences(access, luaContext));
		MethodResult result = doCallMethod(getMethod(method), context, args);

		return unwrap(result, access, luaContext);
	}
}
