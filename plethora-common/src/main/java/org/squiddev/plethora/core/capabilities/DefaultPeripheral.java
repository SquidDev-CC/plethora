package org.squiddev.plethora.core.capabilities;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A default peripheral implementation called "plethora:default_peripheral".
 *
 * This has no methods: it is just a stub for the capability
 */
public final class DefaultPeripheral implements IPeripheral, IPeripheralHandler {
	@Override
	public String getType() {
		return Plethora.ID + ":default_peripheral";
	}

	@Override
	public String[] getMethodNames() {
		return new String[0];
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess access, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) {
		return null;
	}

	@Override
	public void attach(@Nonnull IComputerAccess access) {
	}

	@Override
	public void detach(@Nonnull IComputerAccess access) {
	}

	@Override
	public boolean equals(IPeripheral other) {
		return other == this || other instanceof DefaultPeripheral;
	}

	@Nonnull
	@Override
	public IPeripheral getPeripheral() {
		return this;
	}

	@Override
	public void update(@Nonnull World world, @Nonnull Vec3d position, @Nullable EntityLivingBase entity) {
	}
}
