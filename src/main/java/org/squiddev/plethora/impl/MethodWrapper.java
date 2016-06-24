package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for a list of methods
 */
public class MethodWrapper<T> implements ILuaObject {
	final IUnbakedContext<T> context;
	private final List<IMethod<T>> methods;
	private final String[] names;

	public MethodWrapper(IUnbakedContext<T> context, List<IMethod<T>> methods) {
		this.context = context;
		this.methods = methods;

		String[] names = this.names = new String[methods.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = methods.get(i).getName();
		}
		DebugLogger.debug(Arrays.toString(names));
	}

	@Override
	public String[] getMethodNames() {
		return names;
	}

	@Override
	public Object[] callMethod(ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		return callMethod(context, luaContext, method, args);
	}

	public Object[] callMethod(final IUnbakedContext<T> context, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		final IMethod<T> m = methods.get(method);
		try {
			if (m.worldThread()) {
				return luaContext.executeMainThreadTask(new ILuaTask() {
					@Override
					public Object[] execute() throws LuaException {
						return m.apply(context.bake(), args);
					}
				});
			} else {
				return m.apply(context.bake(), args);
			}
		} catch (RuntimeException e) {
			DebugLogger.error("Unexpected error calling " + m.getName(), e);
			throw new LuaException(e.toString());
		}
	}
}
