package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.id;

/**
 * Handles integration with a {@link IPeripheral}
 */
public class MethodWrapperPeripheral extends MethodWrapper implements IPeripheral {
	private final Object owner;
	private final String type;

	public MethodWrapperPeripheral(Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
		this(owner.getClass().getName(), owner, methods, contexts);
	}

	public MethodWrapperPeripheral(String name, Object owner, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts) {
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
		IUnbakedContext context = getContext(method).withContext(id(access), id(luaContext));
		MethodResult result = doCallMethod(getMethod(method), context, args);

		return unwrap(result, access, luaContext);
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
		if (other == null || !(other instanceof MethodWrapperPeripheral)) return false;

		return owner == ((MethodWrapperPeripheral) other).owner;
	}
}
