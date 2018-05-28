package org.squiddev.plethora.integration.tconstruct;

import dan200.computercraft.api.lua.LuaException;
import jline.internal.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.squiddev.plethora.api.method.*;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.ISmelteryTankHandler;
import slimeknights.tconstruct.smeltery.tileentity.TileHeatingStructureFuelTank;
import slimeknights.tconstruct.smeltery.tileentity.TileSmelteryComponent;
import slimeknights.tconstruct.smeltery.tileentity.TileTank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;

public class MethodsSmeltery {
	@BasicMethod.Inject(
		value = TileSmelteryComponent.class, modId = TConstruct.modID,
		doc = "function(fluid: number|string) -- Select which fluid will be extracted by drains in the smeltery. One can specify a fluid name or an index in list of molten fluids."
	)
	public static MethodResult selectMolten(IUnbakedContext<TileSmelteryComponent> context, Object[] args) throws LuaException {
		if (args.length >= 1 && args[0] instanceof String) {
			String fluid = (String) args[0];
			return MethodResult.nextTick(() -> {
				ISmelteryTankHandler smeltery = getSmeltery(context.bake());

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
				ISmelteryTankHandler smeltery = getSmeltery(context.bake());
				List<FluidStack> fluids = smeltery.getTank().getFluids();
				assertBetween(fluid, 1, fluids.size(), "Fluid out of range (%s)");

				smeltery.getTank().moveFluidToBottom(fluid - 1);
				smeltery.onTankChanged(fluids, null);

				return MethodResult.empty();
			});
		}
	}

	@BasicObjectMethod.Inject(
		value = TileSmelteryComponent.class, modId = TConstruct.modID, worldThread = true,
		doc = "function():table -- Get a list of all molten fluids within the smeltery."
	)
	public static Object[] getMolten(IContext<TileSmelteryComponent> context, Object[] args) throws LuaException {
		ISmelteryTankHandler smeltery = getSmeltery(context);

		Map<Integer, Object> result = new HashMap<>();
		int i = 0;
		for (FluidStack fluid : smeltery.getTank().getFluids()) {
			result.put(++i, context.makePartialChild(fluid).getMeta());
		}

		return new Object[]{result};
	}

	@BasicObjectMethod.Inject(
		value = TileSmelteryComponent.class, modId = TConstruct.modID, worldThread = true,
		doc = "function():table -- Get a list of all fuels currently used by the seared-bricks multiblock."
	)
	public static Object[] getFuels(IContext<TileSmelteryComponent> context, Object[] args) throws LuaException {
		TileHeatingStructureFuelTank<?> structure = getHeatingStructure(context);

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
		value = TileSmelteryComponent.class, modId = TConstruct.modID, worldThread = true,
		doc = "function():number -- Get the internal temperature of this structure."
	)
	public static Object[] getTemperature(IContext<TileSmelteryComponent> context, Object[] args) throws LuaException {
		TileHeatingStructureFuelTank<?> structure = getHeatingStructure(context);
		return new Object[]{structure.getTemperature()};
	}

	private static TileHeatingStructureFuelTank<?> getHeatingStructure(IContext<TileSmelteryComponent> context) throws LuaException {
		TileSmelteryComponent component = context.getTarget();
		if (!component.getHasMaster()) throw new LuaException("Cannot find a controller");

		TileEntity te = getRelatedTile(component, component.getMasterPosition());
		if (!(te instanceof TileHeatingStructureFuelTank)) throw new LuaException("Cannot find a controller");

		return (TileHeatingStructureFuelTank) te;
	}

	private static ISmelteryTankHandler getSmeltery(IContext<TileSmelteryComponent> context) throws LuaException {
		TileSmelteryComponent component = context.getTarget();
		if (!component.getHasMaster()) throw new LuaException("Not part of a smeltery");

		TileEntity te = getRelatedTile(component, component.getMasterPosition());
		if (!(te instanceof ISmelteryTankHandler)) throw new LuaException("Not part of a smeltery");

		return (ISmelteryTankHandler) te;
	}

	@Nullable
	private static TileEntity getRelatedTile(TileEntity component, BlockPos pos) {
		World world = component.getWorld();
		return world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
	}
}
