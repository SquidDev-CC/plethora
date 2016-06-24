package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IContext;

/**
 * Implicit context
 */
public class ComputerCraftContext<T> implements IContext<T> {
	private final T target;
	private final Object[] context;

	private final ILuaContext luaContext;
	private final IComputerAccess access;
	private final boolean complete;

	private ComputerCraftContext(T target, Object[] context, ILuaContext luaContext, IComputerAccess access) {
		this.target = target;
		this.context = context;

		this.luaContext = luaContext;
		this.access = access;
		complete = true;
	}

	public ComputerCraftContext(T target, Object[] context) {
		this.target = target;
		this.context = context;

		this.luaContext = null;
		this.access = null;
		complete = false;
	}

	/**
	 * Add the Lua environment
	 *
	 * @param luaContext The lua context
	 * @param access     The computer access
	 * @return The instantiated context
	 */
	public IContext<T> instantiate(ILuaContext luaContext, IComputerAccess access) {
		return new ComputerCraftContext<T>(target, context, luaContext, access);
	}

	@Override
	public T getTarget() {
		return target;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getEnvironment(Class<V> klass) {
		if (!complete) throw new IllegalStateException("Cannot get context at compile time");

		if (klass == ILuaContext.class) {
			return (V) luaContext;
		} else if (klass == IComputerAccess.class) {
			return (V) access;
		} else {
			return null;
		}
	}

	@Override
	public <V> boolean hasEnvironment(Class<V> klass) {
		return klass == ILuaContext.class || klass == IComputerAccess.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(Class<V> klass) {
		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(Class<V> klass) {
		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return true;
		}

		return false;
	}
}
