package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.config.ConfigHandler;
import com.setycz.chickens.entity.EntityChickensChicken;
import com.setycz.chickens.registry.ChickensRegistry;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleContainerObjectMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

//REFINE Check if I need to annotate each method, or if the new @Injects annotation will work
public class MethodsAnalyzer {
	@ModuleContainerObjectMethod.Inject(
			module = IntegrationChickens.analyzerMod, worldThread = false, modId = ChickensMod.MODID,
			doc = "function():table -- Get a list of all chicken species"
	)
	public static Object[] getSpeciesList(IContext<IModuleContainer> context, Object[] args) {
		//TODO Determine if we need to do any additional manipulation of the collection
		// before returning
		return ChickensRegistry.getItems().toArray();
	}

	/*TODO Determine full set of desired functionality for the Chickens Analyzer
	 *
	 * Public methods from the `ChickensRegistry` class include:
	 * getByResourceLocation
	 * getByRegistryName
	 * getItems - Returns all _enabled_ `ChickensRegistryItem`s
	 *
	 * Other methods feel superfluous at this time; they can be added if requested
	 */

	/*
	//TODO If Chickens requires analyzing an entity to show stats, enable a method that
	// requires both the Chickens analyzer and the Entity Sensor
	// Reference MethodsIntrospection.getMetaOwner
	//TODO Determine the correct method type for this use
	@SubtargetedModuleObjectMethod.Inject(
			module = {PlethoraModules.SENSOR_S, IntegrationChickens.analyzerMod},
			target = EntityChickensChicken.class

	)
	public static MethodResult getChickenStatsByID() {
		if (!ConfigHandler.alwaysShowStats) {
			//TODO Then we need to have the analyzer, otherwise a standard sensor module works
		}

		//FIXME Implement this mess
		return null;
	} */
}
