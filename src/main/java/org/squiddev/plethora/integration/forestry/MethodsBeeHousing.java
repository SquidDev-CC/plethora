package org.squiddev.plethora.integration.forestry;

import com.google.common.collect.Maps;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.IErrorState;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import java.util.Collection;
import java.util.Map;

public class MethodsBeeHousing {
	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the current queen for this bee housing."
	)
	public static Object[] getQueen(IContext<IBeeHousing> context, Object[] arg) {
		ItemStack queen = context.getTarget().getBeeInventory().getQueen();
		return new Object[]{
			queen.isEmpty()
				? null
				: context.makePartialChild(queen).getMeta()
		};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the current drone for this bee housing."
	)
	public static Object[] getDrone(IContext<IBeeHousing> context, Object[] arg) {
		ItemStack drone = context.getTarget().getBeeInventory().getDrone();
		return new Object[]{
			drone.isEmpty()
				? null
				: context.makePartialChild(drone).getMeta()
		};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():string -- Get the temperature of this bee housing."
	)
	public static Object[] getTemperature(IContext<IBeeHousing> context, Object[] arg) {
		return new Object[]{context.getTarget().getTemperature().getName()};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():string -- Get the temperature of this bee housing."
	)
	public static Object[] getHumidity(IContext<IBeeHousing> context, Object[] arg) {
		return new Object[]{context.getTarget().getHumidity().getName()};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the errors which are preventing the bees from working."
	)
	public static Object[] getErrors(IContext<IBeeHousing> context, Object[] arg) {
		Collection<IErrorState> states = context.getTarget().getErrorLogic().getErrorStates();

		int i = 0;
		Map<Integer, String> out = Maps.newHashMapWithExpectedSize(states.size());
		for (IErrorState state : states) {
			out.put(++i, state.getUniqueName());
		}
		return new Object[]{out};
	}
}
