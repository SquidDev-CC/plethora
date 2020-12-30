package org.squiddev.plethora.api.method;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.PlethoraAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Various helper methods for costs
 */
public final class CostHelpers {
	private CostHelpers() {
		throw new IllegalStateException("Cannot instantiate singleton " + getClass().getName());
	}

	/**
	 * Get the cost handler for this object
	 *
	 * @param object The cost handler's owner
	 * @return The associated cost handler
	 */
	@Nonnull
	public static ICostHandler getCostHandler(@Nonnull ICapabilityProvider object) {
		return PlethoraAPI.instance().methodRegistry().getCostHandler(object, null);
	}

	/**
	 * Get the cost handler for this object
	 *
	 * @param object The cost handler's owner
	 * @param side   The side to get the cost handler from.
	 * @return The associated cost handler
	 */
	@Nonnull
	public static ICostHandler getCostHandler(@Nonnull ICapabilityProvider object, @Nullable EnumFacing side) {
		return PlethoraAPI.instance().methodRegistry().getCostHandler(object, side);
	}
}
