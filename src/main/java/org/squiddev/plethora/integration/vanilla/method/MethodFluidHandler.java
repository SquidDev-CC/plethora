package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.optString;

@IMethod.Inject(ICapabilityProvider.class)
public class MethodFluidHandler extends BasicMethod<ICapabilityProvider> {
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
			IFluidHandler handler = baked
				.getTarget()
				.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);

			if (handler == null) return MethodResult.result(Collections.emptyMap());

			IFluidTankProperties[] info = handler.getTankProperties();
			Map<Integer, Object> out = new HashMap<>(info.length);
			for (int i = 0; i < info.length; i++) {
				out.put(i + 1, baked.makePartialChild(info[i]).getMeta());
			}

			return MethodResult.result(out);
		});
	}
}
