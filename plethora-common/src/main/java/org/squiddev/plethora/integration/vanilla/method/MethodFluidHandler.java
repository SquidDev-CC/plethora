package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.BlockReference;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dan200.computercraft.api.lua.ArgumentHelper.optString;

@Injects
public final class MethodFluidHandler extends BasicMethod<ICapabilityProvider> {
	public MethodFluidHandler() {
		super("getTanks", "function([side:string]):table -- Get a list of all tanks on this side");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ICapabilityProvider> context) {
		if (!super.canApply(context)) return false;

		ICapabilityProvider target = context.getTarget();
		if (target.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) return true;

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (target.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) return true;
		}

		return false;
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<ICapabilityProvider> context, @Nonnull Object[] args) throws LuaException {
		String side = optString(args, 0, null);
		final EnumFacing facing;
		if (side != null) {
			side = side.toLowerCase();
			facing = EnumFacing.byName(side);
			if (facing == null) throw new LuaException("Unknown side '" + side + "'");
		} else {
			facing = null;
		}

		return MethodResult.nextTick(() -> {
			IPartialContext<ICapabilityProvider> baked = context.bake();
			ICapabilityProvider provider = baked.getTarget();
			IFluidTankProperties[] tanks = getTanks(provider, facing);

			// If we've no tank, and we're using the null side, try to use the current side we're on.
			if (tanks == null && facing == null) {
				BlockReference location = baked.getContext(ContextKeys.TARGET, BlockReference.class);
				if (location != null && location.getSide() != null) tanks = getTanks(provider, location.getSide());
			}

			if (tanks == null) return MethodResult.result(Collections.emptyMap());

			Map<Integer, Object> out = new HashMap<>(tanks.length);
			for (int i = 0; i < tanks.length; i++) {
				out.put(i + 1, baked.makePartialChild(tanks[i]).getMeta());
			}

			return MethodResult.result(out);
		});
	}

	private static IFluidTankProperties[] getTanks(ICapabilityProvider provider, EnumFacing side) {
		IFluidHandler handler = provider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		if (handler == null) return null;

		IFluidTankProperties[] tanks = handler.getTankProperties();
		return tanks == null || tanks.length == 0 ? null : tanks;
	}
}
