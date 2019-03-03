package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;

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
	 * @param amount The amount to consume. This must be &gt;= 0.
	 * @return If there is sufficient energy
	 */
	boolean consume(double amount);

	/**
	 * Start a method once this cost handler has sufficient energy to consume it.
	 *
	 * @param amount The amount of energy to consume. This must be &gt;= 0.
	 * @param next   The method result to continue with once we have sufficient energy.
	 * @return The method result
	 * @throws LuaException If there will never be sufficient energy.
	 */
	default MethodResult await(double amount, MethodResult next) throws LuaException {
		return await(amount, () -> next);
	}

	/**
	 * Start a method once this cost handler has sufficient energy to consume it.
	 *
	 * @param amount The amount of energy to consume. This must be &gt;= 0.
	 * @param next   The callback to continue with once we have sufficient energy.
	 * @return The method result
	 * @throws LuaException If there will never be sufficient energy.
	 */
	MethodResult await(double amount, Callable<MethodResult> next) throws LuaException;
}
