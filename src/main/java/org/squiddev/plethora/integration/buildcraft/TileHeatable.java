package org.squiddev.plethora.integration.buildcraft;

import buildcraft.api.tiles.IHeatable;
import buildcraft.core.BCCore;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

public final class TileHeatable {

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get the minimal heat.")
	public static double getMinHeat(@FromTarget IHeatable heatable) {
		return heatable.getMinHeatValue();
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get the maximal heat.")
	public static double getMaxHeat(@FromTarget IHeatable heatable) {
		return heatable.getMaxHeatValue();
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get the current heat.")
	public static double getCurrentHeat(@FromTarget IHeatable heatable) {
		return heatable.getCurrentHeatValue();
	}

	@PlethoraMethod(modId = BCCore.MODID, doc = "-- Get the ideal heat.")
	public static double getIdealHeat(@FromTarget IHeatable heatable) {
		return heatable.getIdealHeatValue();
	}
}
