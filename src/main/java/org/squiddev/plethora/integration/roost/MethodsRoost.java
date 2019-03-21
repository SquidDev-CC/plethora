package org.squiddev.plethora.integration.roost;

import com.google.common.collect.Maps;
import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.data.DataChicken;
import com.timwoodcreates.roost.data.DataChickenModded;
import com.timwoodcreates.roost.data.DataChickenVanilla;
import com.timwoodcreates.roost.tileentity.TileEntityBreeder;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.utils.LuaList;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/*
 * When working on this class, ensure that getSpecies and getSpeciesList from chickens/MethodsAnalyzer are kept in lock-step!
 */
public class MethodsRoost {

	@PlethoraMethod(
		modId = Roost.MODID,
		doc = "-- Get a list of chickens species"
	)
	public static Map<Integer, Object> getSpeciesList(@Nonnull IContext<TileEntityBreeder> context) {
		if (Loader.isModLoaded(ChickensMod.MODID)) {
			LuaList<Object> species = ChickensRegistry.getItems().stream()
				.map(m -> context.makePartialChild(m).getMeta())
				.collect(LuaList.toLuaList());

			return species.asMap();
		}

		//FIXME Determine a proper return value
		// If Chickens isn't loaded, then Roost only registers DataChickenVanilla
		// This, in turn, doesn't have any breeding pairs,
		// but it will expose different methods than a ChickensRegistryItem
		return Collections.emptyMap();
	}

	@PlethoraMethod(
		modId = Roost.MODID,
		doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<TileEntityBreeder> context, String name) {

		//FIXME Well crud... Roost and Chickens use different internal names, e.g. Roost's "blue" to Chicken's "chickens:bluechicken"
		// unless this is exposed by the (hidden...) "Chicken" tag... yup, that's the case...

		if (Loader.isModLoaded(ChickensMod.MODID)) {
			ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

			//REFINE Code style review: What style is preferred?
			// The current structure?
			// Flipping the conditional? (E.g. "species != null")
			// Ternary operator?
			if (species == null) return Collections.emptyMap();

			return context.makePartialChild(species).getMeta();
		}

		//FIXME Determine a proper return value
		// If Chickens isn't loaded, then Roost only registers DataChickenVanilla
		// This, in turn, doesn't have any breeding pairs,
		// but it will expose different methods than a ChickensRegistryItem
		return Collections.emptyMap();
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
