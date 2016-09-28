package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.Callable;

@IMethod.Inject(IFluidHandler.class)
public class MethodFluidHandler extends BasicMethod<IFluidHandler> {
	public MethodFluidHandler() {
		super("getTanks", "function(side:string):table -- Get a list of all tanks on this side");
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<IFluidHandler> context, @Nonnull Object[] args) throws LuaException {
		String side = ArgumentHelper.optString(args, 0, null);
		final EnumFacing facing;
		if (side != null) {
			side = side.toLowerCase();
			facing = EnumFacing.byName(side);
			if (facing == null) throw new LuaException("Unknown side '" + side + "'");
		} else {
			facing = null;
		}

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				Map<Integer, Object> out = Maps.newHashMap();
				IPartialContext<IFluidHandler> baked = context.bake();
				FluidTankInfo[] info = baked.getTarget().getTankInfo(facing);

				for (int i = 0; i < info.length; i++) {
					out.put(i + 1, baked.makePartialChild(info[i]).getMeta());
				}

				return MethodResult.result(out);
			}
		});
	}
}
