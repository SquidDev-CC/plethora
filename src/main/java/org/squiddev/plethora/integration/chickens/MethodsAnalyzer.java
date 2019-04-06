package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * When working on this class, ensure that getSpecies and getSpeciesList from roost/MethodsRoost are kept in lock-step!
 */
public final class MethodsAnalyzer {
	private MethodsAnalyzer() {
	}

	@PlethoraMethod(
		module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
		doc = "-- Get a list of all chicken species, with the species name as the index"
	)
	public static Map<String, Object> getSpeciesList(@Nonnull IContext<IModuleContainer> context) {
		return ChickensRegistry.getItems().stream()
			.collect(Collectors.toMap(item -> item.getRegistryName().toString(),
				item -> context.makePartialChild(item).getMeta(),
				(a, b) -> b));
	}

	@Nonnull
	@PlethoraMethod(
		module = IntegrationChickens.ANALYZER_S, modId = ChickensMod.MODID,
		doc = "-- Get a single chicken species"
	)
	public static Map<String, ?> getSpecies(@Nonnull IContext<IModuleContainer> context, String name) {
		ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

		return species != null
			? context.makePartialChild(species).getMeta()
			: Collections.emptyMap();

	}
}
