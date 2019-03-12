package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ITransferMethod;
import org.squiddev.plethora.api.method.MarkerInterfaces;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Various methods for fluid transfer
 */
public class MethodsFluidTransfer {
	@PlethoraMethod(doc = "-- Push fluid from this tank to another tank. Returns the amount transferred.")
	@MarkerInterfaces(ITransferMethod.class)
	public static int pushFluid(
		IContext<IFluidHandler> context,
		String toName, @Optional(defInt = Integer.MAX_VALUE) int limit, Fluid fluid
	) throws LuaException {
		IFluidHandler from = context.getTarget();

		// Find location to transfer to
		Object location = context.getTransferLocation(toName);
		if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

		IFluidHandler to = extractHandler(location);
		if (to == null) throw new LuaException("Target '" + toName + "' is not an tank");

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return fluid == null
			? moveFluid(from, limit, to)
			: moveFluid(from, new FluidStack(fluid, limit), to);
	}

	@PlethoraMethod(doc = "-- Pull fluid to this tank from another tank. Returns the amount transferred.")
	@MarkerInterfaces(ITransferMethod.class)
	public static int pullFluid(
		IContext<IFluidHandler> context,
		String fromName, @Optional(defInt = Integer.MAX_VALUE) int limit, @Optional Fluid fluid
	) throws LuaException {
		IFluidHandler to = context.getTarget();

		// Find location to transfer to
		Object location = context.getTransferLocation(fromName);
		if (location == null) throw new LuaException("Source '" + fromName + "' does not exist");

		IFluidHandler from = extractHandler(location);
		if (from == null) throw new LuaException("Source '" + fromName + "' is not an inventory");

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		return fluid == null
			? moveFluid(from, limit, to)
			: moveFluid(from, new FluidStack(fluid, limit), to);
	}

	@Nullable
	private static IFluidHandler extractHandler(@Nonnull Object object) {
		for (Object child : PlethoraAPI.instance().converterRegistry().convertAll(object)) {
			if (child instanceof IFluidHandler) return (IFluidHandler) child;

			if (object instanceof ICapabilityProvider) {
				IFluidHandler handler = ((ICapabilityProvider) object).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
				if (handler != null) return handler;

				handler = ((ICapabilityProvider) object).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				if (handler != null) return handler;
			}
		}

		return null;
	}

	/**
	 * Move fluid from one handler to another
	 *
	 * @param from  The handler to move from
	 * @param limit The maximum amount of fluid to move
	 * @param to    The handler to move to
	 * @return The actual number moved
	 */
	private static int moveFluid(IFluidHandler from, int limit, IFluidHandler to) {
		// See how much we can get out of this tank
		return moveFluid(from, from.drain(limit, false), limit, to);
	}

	/**
	 * Move fluid from one handler to another
	 *
	 * @param from  The handler to move from
	 * @param fluid The fluid and limit to move
	 * @param to    The handler to move to
	 * @return The actual number moved
	 */
	private static int moveFluid(IFluidHandler from, FluidStack fluid, IFluidHandler to) {
		// See how much we can get out of this tank
		return moveFluid(from, from.drain(fluid, false), fluid.amount, to);
	}

	/**
	 * Move fluid from one handler to another
	 *
	 * @param from      The handler to move from
	 * @param extracted The fluid which is extracted from {@code from}
	 * @param limit     The maximum amount of fluid to move
	 * @param to        The handler to move to
	 * @return The actual number moved
	 */
	private static int moveFluid(IFluidHandler from, FluidStack extracted, int limit, IFluidHandler to) {
		if (extracted == null || extracted.amount <= 0) return 0;
		extracted = extracted.copy();

		// Limit the amount to extract
		extracted.amount = Math.min(extracted.amount, limit);

		// Insert into the new handler
		int inserted = to.fill(extracted, true);
		if (inserted <= 0) return 0;

		// Technically this could fail. If it does then I'm going to cry.
		extracted.amount = inserted;
		from.drain(extracted, true);

		return inserted;
	}
}
