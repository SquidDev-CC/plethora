package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.List;

/**
 * Wrapper for a list of methods
 */
class MethodWrapper<T> implements ILuaObject {
	final Context<T> context;
	private final List<IMethod<T>> methods;
	private final String[] names;

	public MethodWrapper(Context<T> context, List<IMethod<T>> methods) {
		this.context = context;
		this.methods = methods;

		String[] names = this.names = new String[methods.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = methods.get(i).getName();
		}
	}

	public MethodWrapper(Context<T> context) {
		this(context, MethodRegistry.instance.getMethods(context));
	}

	@Override
	public String[] getMethodNames() {
		return names;
	}

	@Override
	public Object[] callMethod(ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		return callMethod(context, luaContext, method, args);
	}

	protected Object[] callMethod(final Context<T> context, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		final IMethod<T> m = methods.get(method);
		try {
			if (m.worldThread()) {
				return luaContext.executeMainThreadTask(new ILuaTask() {
					@Override
					public Object[] execute() throws LuaException {
						return m.apply(context, args);
					}
				});
			} else {
				return m.apply(context, args);
			}
		} catch (RuntimeException e) {
			DebugLogger.error("Unexpected error calling " + m.getName(), e);
			throw new LuaException(e.toString());
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || !(other instanceof MethodWrapper)) return false;

		MethodWrapper<?> otherW = (MethodWrapper) other;
		return context.getTarget().equals(otherW.context.getTarget());
	}
}
