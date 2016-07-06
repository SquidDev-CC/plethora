package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;

import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.id;

/**
 * Wrapper that packages environment
 */
public class PeripheralMethodWrapper extends MethodWrapper implements IPeripheral {
	private final Object owner;
	private final String type;

	public PeripheralMethodWrapper(Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		this(owner.getClass().getCanonicalName(), owner, methods, contexts);
	}

	public PeripheralMethodWrapper(String name, Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		super(methods, contexts);
		this.owner = owner;
		this.type = name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		return callMethod(getContext(method).withContext(id(access), id(luaContext)), luaContext, getMethod(method), args);
	}

	@Override
	public void attach(IComputerAccess access) {
	}

	@Override
	public void detach(IComputerAccess access) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (this == other) return true;
		if (other == null || !(other instanceof PeripheralMethodWrapper)) return false;

		return owner == ((PeripheralMethodWrapper) other).owner;
	}
}
