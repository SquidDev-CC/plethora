package org.squiddev.plethora.utils;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A capability provider which always restricts access to a specific side.
 */
public final class CapabilityWrapper implements ICapabilityProvider {
	private final ICapabilityProvider child;
	private final EnumFacing side;

	public CapabilityWrapper(ICapabilityProvider child, EnumFacing side) {
		this.child = child;
		this.side = side;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing == null || facing == side) && child.hasCapability(capability, side);
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return facing == null || facing == side ? child.getCapability(capability, side) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CapabilityWrapper)) return false;
		CapabilityWrapper that = (CapabilityWrapper) o;
		return child.equals(that.child) && side == that.side;

	}

	@Override
	public int hashCode() {
		return 31 * child.hashCode() + side.hashCode();
	}
}
