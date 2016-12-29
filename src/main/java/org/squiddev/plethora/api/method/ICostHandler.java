package org.squiddev.plethora.api.method;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Stores a value that regenerates over time.
 *
 * This is used to limit the rate methods are called.
 * This can be bound as a capability to an object.
 *
 * You should aim to regenerate the energy level when possible
 *
 * @see org.squiddev.plethora.api.Constants#COST_HANDLER_CAPABILITY
 * @see IMethodRegistry#getCostHandler(ICapabilityProvider, net.minecraft.util.EnumFacing)
 */
@ThreadSafe
public interface ICostHandler {
	/**
	 * Get the current energy level.
	 *
	 * @return The current energy level
	 */
	double get();

	/**
	 * Consume a set amount of energy
	 *
	 * @param amount The amount to consume. This must be >= 0.
	 * @return If there is sufficient energy
	 */
	boolean consume(double amount);
}
