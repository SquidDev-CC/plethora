package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.utils.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/*
 * When working on this class, ensure that getSpecies and getSpeciesList from roost/MethodsRoost are kept in lock-step!
 */
public class MethodsAnalyzer {

	@PlethoraMethod(
		module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
		doc = "-- Get a list of all chicken species"
	)
	public static Map<Integer, Object> getSpeciesList(@Nonnull IContext<IModuleContainer> context) {
		LuaList<Object> species = ChickensRegistry.getItems().stream()
			.map(m -> context.makePartialChild(m).getMeta())
			.collect(LuaList.toLuaList());

		//REFINE May want this to use species names as keys, rather than 1-indexed integers
		return species.asMap();
	}

	@Nonnull
	@PlethoraMethod(
		module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
		doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<IModuleContainer> context, String name) {
		ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

		return species == null ? Collections.emptyMap()
			: context.makePartialChild(species).getMeta();

	}
}
