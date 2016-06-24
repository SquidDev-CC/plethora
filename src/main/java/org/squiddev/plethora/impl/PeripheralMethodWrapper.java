package org.squiddev.plethora.impl;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;

import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.id;

/**
 * Wrapper that packages environment
 */
public class PeripheralMethodWrapper extends MethodWrapper<TileEntity> implements IPeripheral {
	private final TileEntity tile;
	private final String type;

	public PeripheralMethodWrapper(TileEntity tile, IUnbakedContext<TileEntity> context, List<IMethod<TileEntity>> methods) {
		super(context, methods);
		this.tile = tile;
		this.type = tile.getClass().getCanonicalName();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object[] callMethod(IComputerAccess access, ILuaContext luaContext, int method, final Object[] args) throws LuaException, InterruptedException {
		return callMethod(context.withContext(id(access), id(luaContext)), luaContext, method, args);
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

		return tile == ((PeripheralMethodWrapper) other).tile;
	}
}
