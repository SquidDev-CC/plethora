package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.Map;

public class MethodsAnalyzer {

	@PlethoraMethod(
			module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
			doc = "-- Get a list of all chicken species"
	)
	public static Map<Integer, Object> getSpeciesList(@Nonnull IContext<IModuleContainer> context) {
		return ChickensHelper.getSpeciesListHelper(context);
	}

	@Nonnull
	@PlethoraMethod(
			module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
			doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<IModuleContainer> context, String name) {
		return ChickensHelper.getSpeciesHelper(context, name);
	}

	//TODO Doesn't show in getDocs... what'd I break?
	//REFINE This may be problematic if we want to allow captured chickens to be analyzed...
	// Or we may just leave that as a challenge to players.  I plan on using roost for performance.
	@PlethoraMethod(
			module = {PlethoraModules.SENSOR_S}, modId = ChickensMod.MODID,
			doc = "-- Analyze a chicken's stats; once analyzed, check meta as usual"
	)
	public static void analyzeChicken(@Nonnull IPartialContext<EntityChickensChicken> context) throws LuaException {
		// Check if a chicken even _needs_ to be analyzed
		if (ConfigHandler.alwaysShowStats) return;

		// The more I work on it, the more I question the structure of this method...
		if (!context.getModules().hasModule(IntegrationChickens.ANALYZER_MOD)){
			throw new LuaException("Chickens Analyzer required!");
		}

		EntityChickensChicken chicken = context.getTarget();

		// Not sure if this actually protects anything, or just adds overhead...
		if (!chicken.getStatsAnalyzed()) {
			chicken.setStatsAnalyzed(true);
		}

	}
}
