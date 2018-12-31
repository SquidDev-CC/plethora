package org.squiddev.plethora.integration.tconstruct;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.reference.DynamicReference;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.ISmelteryTankHandler;
import slimeknights.tconstruct.smeltery.tileentity.TileHeatingStructureFuelTank;
import slimeknights.tconstruct.smeltery.tileentity.TileMultiblock;
import slimeknights.tconstruct.smeltery.tileentity.TileSmelteryComponent;
import slimeknights.tconstruct.smeltery.tileentity.TileTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

public class MethodsSmeltery {
	@BasicMethod.Inject(
		value = ISmelteryTankHandler.class, modId = TConstruct.modID,
		doc = "function(fluid: number|string) -- Select which fluid will be extracted by drains in the smeltery. One can specify a fluid name or an index in list of molten fluids."
	)
	public static MethodResult selectMolten(IUnbakedContext<ISmelteryTankHandler> context, Object[] args) throws LuaException {
		if (args.length >= 1 && args[0] instanceof String) {
			String fluid = (String) args[0];
			return MethodResult.nextTick(() -> {
				ISmelteryTankHandler smeltery = context.bake().getTarget();

				List<FluidStack> fluids = smeltery.getTank().getFluids();
				for (int i = 0; i < fluids.size(); i++) {
					Fluid stack = fluids.get(i).getFluid();
					if (stack.getName().equalsIgnoreCase(fluid) || FluidRegistry.getDefaultFluidName(stack).equalsIgnoreCase(fluid)) {
						smeltery.getTank().moveFluidToBottom(i);
						smeltery.onTankChanged(fluids, null);
						return MethodResult.empty();
					}
				}

				throw new LuaException(String.format("Cannot find fluid '%s'", fluid));
			});
		} else {
			int fluid = getInt(args, 0);
			return MethodResult.nextTick(() -> {
				ISmelteryTankHandler smeltery = context.bake().getTarget();
				List<FluidStack> fluids = smeltery.getTank().getFluids();
				assertBetween(fluid, 1, fluids.size(), "Fluid out of range (%s)");

				smeltery.getTank().moveFluidToBottom(fluid - 1);
				smeltery.onTankChanged(fluids, null);

				return MethodResult.empty();
			});
		}
	}

	@BasicObjectMethod.Inject(
		value = ISmelteryTankHandler.class, modId = TConstruct.modID,
		doc = "function():table -- Get a list of all molten fluids within the smeltery."
	)
	public static Object[] getMolten(IContext<ISmelteryTankHandler> context, Object[] args) {
		ISmelteryTankHandler smeltery = context.getTarget();

		Map<Integer, Object> result = new HashMap<>();
		int i = 0;
		for (FluidStack fluid : smeltery.getTank().getFluids()) {
			result.put(++i, context.makePartialChild(fluid).getMeta());
		}

		return new Object[]{result};
	}

	@BasicObjectMethod.Inject(
		value = TileHeatingStructureFuelTank.class, modId = TConstruct.modID,
		doc = "function():table -- Get a list of all fuels currently used by the seared-bricks multiblock."
	)
	public static Object[] getFuels(IContext<TileHeatingStructureFuelTank> context, Object[] args) {
		TileHeatingStructureFuelTank<?> structure = context.getTarget();

		Map<Integer, Object> result = new HashMap<>();
		int i = 0;
		for (BlockPos pos : structure.tanks) {
			TileEntity te = getRelatedTile(structure, pos);
			if (!(te instanceof TileTank)) continue;

			IFluidTank internal = ((TileTank) te).getInternalTank();
			if (internal != null && internal.getFluid() != null && TinkerRegistry.isSmelteryFuel(internal.getFluid())) {
				result.put(++i, context.makePartialChild(internal).getMeta());
			}
		}

		return new Object[]{result};
	}

	@BasicObjectMethod.Inject(
		value = TileHeatingStructureFuelTank.class, modId = TConstruct.modID,
		doc = "function():number -- Get the internal temperature of this structure."
	)
	public static Object[] getTemperature(IContext<TileHeatingStructureFuelTank> context, Object[] args) {
		TileHeatingStructureFuelTank<?> structure = context.getTarget();
		return new Object[]{structure.getTemperature()};
	}

	@BasicObjectMethod.Inject(
		value = TileSmelteryComponent.class, modId = TConstruct.modID,
		doc = "function():table|nil -- Get the controller for this smeltery component."
	)
	public static Object[] getController(IContext<TileSmelteryComponent> context, Object[] args) {
		TileSmelteryComponent component = context.getTarget();
		if (!component.getHasMaster()) return null;

		TileEntity te = getRelatedTile(component, component.getMasterPosition());
		if (!(te instanceof TileMultiblock)) return null;

		return new Object[]{context.makeChild((TileMultiblock) te, new ControllerReference(component, (TileMultiblock) te)).getObject()};
	}

	private static class ControllerReference extends DynamicReference<TileMultiblock> {
		private final TileSmelteryComponent origin;
		private final TileMultiblock<?> expected;

		private ControllerReference(TileSmelteryComponent origin, TileMultiblock<?> expected) {
			this.origin = origin;
			this.expected = expected;
		}

		@Nonnull
		@Override
		public TileMultiblock get() throws LuaException {
			if (!origin.getHasMaster() || getRelatedTile(origin, origin.getMasterPosition()) != expected) {
				throw new LuaException("The controller has changed");
			}

			return expected;
		}

		@Nonnull
		@Override
		public TileMultiblock safeGet() {
			return expected;
		}
	}

	@Nullable
	private static TileEntity getRelatedTile(TileEntity component, BlockPos pos) {
		World world = component.getWorld();
		return world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
	}
}
