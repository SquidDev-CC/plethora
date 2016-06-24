package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.plethora.api.method.IMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper that packages environment
 */
public class PeripheralMethodWrapper<T> extends MethodWrapper<T> implements IPeripheral {
	public PeripheralMethodWrapper(Context<T> context, List<IMethod<T>> iMethods) {
		super(context, iMethods);
	}

	public PeripheralMethodWrapper(Context<T> context) {
		super(context);
	}

	@Override
	public String getType() {
		return context.getTarget().getClass().getCanonicalName();
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		Object[] existing = context.getContext();
		Object[] additional = Arrays.copyOf(existing, existing.length + 2);
		additional[existing.length] = access;
		additional[existing.length + 1] = luaContext;

		return callMethod(new Context<T>(context.getTarget(), additional), luaContext, method, args);
	}

	@Override
	public void attach(IComputerAccess access) {
	}

	@Override
	public void detach(IComputerAccess access) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		return super.equals(other);
	}
}
