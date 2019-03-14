package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.utils.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class MethodsAnalyzer {

	@PlethoraMethod(
			module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
			doc = "-- Get a list of all chicken species"
	)
	public static Map<Integer, Object> getSpeciesList(@Nonnull IContext<IModuleContainer> context) {
		LuaList<Object> species = ChickensRegistry.getItems().stream()
				.map(m -> context.makePartialChild(m).getMeta())
				.collect(LuaList.toLuaList());

		return species.asMap();
	}

	@Nonnull
	@PlethoraMethod(
			module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
			doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<IModuleContainer> context, String name) {
		ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

		//REFINE Code style review: What style is preferred?
		// The current structure?
		// Flipping the conditional? (E.g. "species != null")
		// Ternary operator?
		if (species == null) return Collections.emptyMap();

		return context.makePartialChild(species).getMeta();
	}

	//TODO Doesn't show in getDocs... what'd I break?
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
