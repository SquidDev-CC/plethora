package org.squiddev.plethora.integration.forestry;

import forestry.api.apiculture.IBeeHousing;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Map;

public class MethodsBeeHousing {
	@Optional
	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get the current queen for this bee housing.")
	public static Map<Object, Object> getQueen(IContext<IBeeHousing> context) {
		ItemStack queen = context.getTarget().getBeeInventory().getQueen();
		return queen.isEmpty()
			? null
			: context.makePartialChild(queen).getMeta();
	}

	@Optional
	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get the current drone for this bee housing.")
	public static Map<Object, Object> getDrone(IContext<IBeeHousing> context) {
		ItemStack drone = context.getTarget().getBeeInventory().getDrone();
		return drone.isEmpty()
			? null
			: context.makePartialChild(drone).getMeta();
	}

	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get the temperature of this bee housing.")
	public static String getTemperature(@FromTarget IBeeHousing housing) {
		return housing.getTemperature().getName();
	}

	@PlethoraMethod(modId = Constants.MOD_ID, doc = "-- Get the temperature of this bee housing.")
	public static String getHumidity(@FromTarget IBeeHousing housing) {
		return housing.getHumidity().getName();
	}
}
