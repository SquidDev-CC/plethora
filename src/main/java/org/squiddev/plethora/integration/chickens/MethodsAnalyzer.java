package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;

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
}
