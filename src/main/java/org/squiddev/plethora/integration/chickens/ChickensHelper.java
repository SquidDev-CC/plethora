package org.squiddev.plethora.integration.chickens;

import com.google.common.collect.Maps;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.utils.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class ChickensHelper {

	//FIXME Review generics, with the goal of having MethodsRoost.getSpeciesList point to this method
	// so that if we have to change the code, it only needs changed _once_
	// (Aside from IContext<T>, both have the exact same code)
	public static Map<Integer, Object> getSpeciesListHelper(@Nonnull IContext<IModuleContainer> context) {
		LuaList<Object> species = ChickensRegistry.getItems().stream()
				.map(m -> context.makePartialChild(m).getMeta())
				.collect(LuaList.toLuaList());

		return species.asMap();
	}

	//FIXME Same issue as above
	@Nonnull
	public static Map<Object, Object> getSpeciesHelper(@Nonnull IContext<IModuleContainer> context, String name) {
		ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

		//REFINE Code style review: What style is preferred?
		// The current structure?
		// Flipping the conditional? (E.g. "species != null")
		// Ternary operator?
		if (species == null) return Collections.emptyMap();

		return context.makePartialChild(species).getMeta();
	}


	public static Map<Object, Object> getVanillaChicken() {
		Map<Object, Object> out = Maps.newHashMap();

		/* The map for ChickensRegistryItem's currently contains the following:
		 * dropItem: {ItemStack.getMeta}
		 * layItem: {ItemStack.getMeta}
		 * entityName: "WhiteChicken"
		 * name: "chickens:whitechicken"
		 * tier = 1
		 */

		//FIXME Actually implement this
		// Will probably need a Context of some sort...
		return Collections.emptyMap();
	}
}
