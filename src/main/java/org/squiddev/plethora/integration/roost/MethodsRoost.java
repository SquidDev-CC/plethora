package org.squiddev.plethora.integration.roost;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.registry.ChickensRegistry;
import com.setycz.chickens.registry.ChickensRegistryItem;
import com.timwoodcreates.roost.Roost;
import com.timwoodcreates.roost.tileentity.TileEntityBreeder;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * When working on this class, ensure that getSpecies and getSpeciesList from chickens/MethodsAnalyzer are kept in lock-step!
 */
public final class MethodsRoost {

	//I dislike magic values... at least this makes it so that, if for whatever reason,
	//this needs updated, we only change it once...
	private static final String MINECRAFT_CHICKEN = "minecraft:chicken";

	private MethodsRoost() {
	}

	@PlethoraMethod(
		modId = Roost.MODID,
		doc = "-- Get a list of all chickens species, with the species name as the index"
	)
	public static Map<String, Object> getSpeciesList(@Nonnull IContext<TileEntityBreeder> context) {
		Map<Object, Object> vanillaChicken = getVanillaChicken(context);

		if (Loader.isModLoaded(ChickensMod.MODID)) {
			Map<String, Object> species = ChickensRegistry.getItems().stream()
				.collect(Collectors.toMap(item -> item.getRegistryName().toString(),
					item -> context.makePartialChild(item).getMeta(),
					(a, b) -> b));

			species.put(MINECRAFT_CHICKEN, vanillaChicken);

			return species;
		}

		return Collections.singletonMap(MINECRAFT_CHICKEN, vanillaChicken);
	}

	@PlethoraMethod(
		modId = Roost.MODID,
		doc = "-- Get a single chicken species"
	)
	public static Map<Object, Object> getSpecies(@Nonnull IContext<TileEntityBreeder> context, String name) {
		if (MINECRAFT_CHICKEN.equals(name)) return getVanillaChicken(context);

		if (Loader.isModLoaded(ChickensMod.MODID)) {
			ChickensRegistryItem species = ChickensRegistry.getByRegistryName(name);

			return species != null
				? context.makePartialChild(species).getMeta()
				: Collections.emptyMap();
		}

		return Collections.emptyMap();
	}

	private static Map<Object, Object> getVanillaChicken(@Nonnull IPartialContext<?> context) {
		Map<Object, Object> out = new HashMap<>();

		out.put("type", MINECRAFT_CHICKEN);
		out.put("tier", 0);

		//Technically, it has a chance for either eggs or feathers...
		out.put("layItem", ContextHelpers.wrapStack(context, new ItemStack(Items.FEATHER)));

		//Leaving `null` for now
		out.put("dropItem", null);

		out.put("parent1", null);
		out.put("parent2", null);

		return out;
	}

}
