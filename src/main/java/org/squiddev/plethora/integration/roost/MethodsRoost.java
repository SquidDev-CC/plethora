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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
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
		Map<Object, Object> vanillaChicken = getVanillaChicken(context);

		if (Loader.isModLoaded(ChickensMod.MODID)) {
			LuaList<Object> species = ChickensRegistry.getItems().stream()
				.map(m -> context.makePartialChild(m).getMeta())
				.collect(LuaList.toLuaList());

			species.add(vanillaChicken);

			return species.asMap();
		}

		return Collections.singletonMap(1, vanillaChicken);
	}

	@PlethoraMethod(
		modId = Roost.MODID,
		doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<TileEntityBreeder> context, String name) {
		if ("minecraft:chicken".equals(name)) return getVanillaChicken(context);

		if (Loader.isModLoaded(ChickensMod.MODID)) {
			ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

			return species != null
				? context.makePartialChild(species).getMeta()
				: Collections.emptyMap();
		}

		return Collections.emptyMap();
	}

	private static Map<Object, Object> getVanillaChicken(@Nonnull IPartialContext context) {
		Map<Object, Object> out = Maps.newHashMap();

		out.put("type", "minecraft:chicken");
		out.put("tier", 0);

		//Technically, it has a chance for either eggs or feathers...
		out.put("layItem", context.makePartialChild(new ItemStack(Items.FEATHER)).getMeta());

		//TODO Determine what value we want for this entry
		out.put("dropItem", null);

		out.put("parent1", null);
		out.put("parent2", null);

		return out;
	}

}
