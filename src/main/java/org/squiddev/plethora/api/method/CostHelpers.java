package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;
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
	 * Consume a set amount of energy, error if not possible
	 *
	 * @param handler The cost handler to use
	 * @param amount  The amount to consume. This must be > 0.
	 * @throws LuaException If there is insufficient energy
	 */
	public static void checkCost(@Nonnull ICostHandler handler, double amount) throws LuaException {
		if (!handler.consume(amount)) {
			throw new LuaException("Insufficient energy (requires " + amount + ", has " + handler.get() + ".");
		}
	}

	/**
	 * Consume a set amount of energy, error if not possible
	 *
	 * @param handler The cost handler to use
	 * @param amount  The amount to consume. This must be > 0.
	 * @param message The message to throw with
	 * @throws LuaException If there is insufficient energy
	 */
	public static void checkCost(@Nonnull ICostHandler handler, double amount, @Nonnull String message) throws LuaException {
		if (!handler.consume(amount)) {
			throw new LuaException(message);
		}
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
