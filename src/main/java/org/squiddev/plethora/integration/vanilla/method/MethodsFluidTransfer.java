package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

/**
 * Various methods for fluid transfer
 */
public class MethodsFluidTransfer {
	@BasicMethod.Inject(
		value = IFluidHandler.class,
		doc = "function(to:string[, limit:int [, fluid:string]]):int -- Push fluid from this tank to another tank. Returns the amount transferred."
	)
	@MarkerInterfaces(ITransferMethod.class)
	public static MethodResult pushFluid(final IUnbakedContext<IFluidHandler> context, Object[] args) throws LuaException {
		final String toName = getString(args, 0);
		final int limit = optInt(args, 1, Integer.MAX_VALUE);
		final String fluidName = optString(args, 2, null);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		Fluid fluid;
		if (fluidName != null) {
			fluid = FluidRegistry.getFluid(fluidName);
			if (fluid == null) throw new LuaException("Unknown fluid '" + fluidName + "'");
		} else {
			fluid = null;
		}

		return MethodResult.nextTick(() -> {
			IContext<IFluidHandler> baked = context.bake();
			IFluidHandler from = baked.getTarget();

			// Find location to transfer to
			Object location = baked.getTransferLocation(toName);
			if (location == null) throw new LuaException("Target '" + toName + "' does not exist");

			IFluidHandler to = extractHandler(location);
			if (to == null) throw new LuaException("Target '" + toName + "' is not an tank");

			return MethodResult.result(fluid == null
				? moveFluid(from, limit, to)
				: moveFluid(from, new FluidStack(fluid, limit), to)
			);
		});
	}

	@BasicMethod.Inject(
		value = IFluidHandler.class,
		doc = "function(from:string[, limit:int [, fluid:string]]):int -- Pull fluid to this tank from another tank. Returns the amount transferred."
	)
	@MarkerInterfaces(ITransferMethod.class)
	public static MethodResult pullFluid(final IUnbakedContext<IFluidHandler> context, Object[] args) throws LuaException {
		final String fromName = getString(args, 0);
		final int limit = optInt(args, 1, Integer.MAX_VALUE);
		final String fluidName = optString(args, 2, null);

		if (limit <= 0) throw new LuaException("Limit must be > 0");

		Fluid fluid;
		if (fluidName != null) {
			fluid = FluidRegistry.getFluid(fluidName);
			if (fluid == null) throw new LuaException("Unknown fluid '" + fluidName + "'");
		} else {
			fluid = null;
		}

		return MethodResult.nextTick(() -> {
			IContext<IFluidHandler> baked = context.bake();
			IFluidHandler to = baked.getTarget();

			// Find location to transfer to
			Object location = baked.getTransferLocation(fromName);
			if (location == null) throw new LuaException("Source '" + fromName + "' does not exist");

			IFluidHandler from = extractHandler(location);
			if (from == null) throw new LuaException("Source '" + fromName + "' is not an inventory");

			return MethodResult.result(fluid == null
				? moveFluid(from, limit, to)
				: moveFluid(from, new FluidStack(fluid, limit), to)
			);
		});
	}

	@Nullable
	public static IFluidHandler extractHandler(@Nonnull Object object) {
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
