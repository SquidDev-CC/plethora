package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.List;

/**
 * Wrapper for a list of methods
 */
public class MethodWrapper implements ILuaObject {
	private final List<IMethod<?>> methods;
	private final List<IUnbakedContext<?>> contexts;

	private final String[] names;

	public MethodWrapper(List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		this.contexts = contexts;
		this.methods = methods;

		String[] names = this.names = new String[methods.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = methods.get(i).getName();
		}
	}

	@Override
	public String[] getMethodNames() {
		return names;
	}

	@Override
	public Object[] callMethod(ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		return callMethod(getContext(method), luaContext, getMethod(method), args);
	}

	public IMethod<?> getMethod(int i) {
		return methods.get(i);
	}

	public IUnbakedContext<?> getContext(int i) {
		return contexts.get(i);
	}

	private static final class ObjectCell {
		public Object[] value;
	}

	public static Object[] callMethod(final IUnbakedContext context, ILuaContext luaContext, final IMethod method, final Object[] args) throws LuaException, InterruptedException {
		if (method.worldThread()) {
			// We do this magic to prevent ILuaObject being converted to functions then nil.
			final ObjectCell cell = new ObjectCell();
			luaContext.executeMainThreadTask(new ILuaTask() {
				@Override
				public Object[] execute() throws LuaException {
					cell.value = doCallMethod(method, context, args);
					return null;
				}
			});
			return cell.value;
		} else {
			return doCallMethod(method, context, args);
		}
	}

	@SuppressWarnings("unchecked")
	private static Object[] doCallMethod(IMethod method, IUnbakedContext context, Object[] args) throws LuaException {
		try {
			return method.apply(context.bake(), args);
		} catch (RuntimeException e) {
			DebugLogger.error("Unexpected error calling " + method.getName(), e);
			throw new LuaException("Java Exception Thrown: " + e.toString());
		}
	}
}
