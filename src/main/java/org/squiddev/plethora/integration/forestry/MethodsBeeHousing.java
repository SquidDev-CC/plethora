package org.squiddev.plethora.integration.forestry;

import com.google.common.collect.Maps;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.core.IErrorState;
import forestry.api.genetics.*;
import forestry.core.config.Constants;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MethodsBeeHousing {
	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the current queen for this bee housing."
	)
	public static Object[] getQueen(IContext<IBeeHousing> context, Object[] arg) {
		ItemStack queen = context.getTarget().getBeeInventory().getQueen();
		return new Object[]{
			queen.isEmpty()
				? null
				: context.makePartialChild(queen).getMeta()
		};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the current drone for this bee housing."
	)
	public static Object[] getDrone(IContext<IBeeHousing> context, Object[] arg) {
		ItemStack drone = context.getTarget().getBeeInventory().getDrone();
		return new Object[]{
			drone.isEmpty()
				? null
				: context.makePartialChild(drone).getMeta()
		};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():string -- Get the temperature of this bee housing."
	)
	public static Object[] getTemperature(IContext<IBeeHousing> context, Object[] arg) {
		return new Object[]{context.getTarget().getTemperature().getName()};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():string -- Get the temperature of this bee housing."
	)
	public static Object[] getHumidity(IContext<IBeeHousing> context, Object[] arg) {
		return new Object[]{context.getTarget().getHumidity().getName()};
	}

	@BasicObjectMethod.Inject(
		value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = true,
		doc = "function():table -- Get the errors which are preventing the bees from working."
	)
	public static Object[] getErrors(IContext<IBeeHousing> context, Object[] arg) {
		Collection<IErrorState> states = context.getTarget().getErrorLogic().getErrorStates();

		int i = 0;
		Map<Integer, String> out = Maps.newHashMapWithExpectedSize(states.size());
		for (IErrorState state : states) {
			out.put(++i, state.getUniqueName());
		}
		return new Object[]{out};
	}

	private static ISpeciesRoot getBeeRoot() {
		return AlleleManager.alleleRegistry.getSpeciesRoot("rootBees");
	}

	@BasicObjectMethod.Inject(
			value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = false,
			doc = "function():table -- Get a list of all bee species"
	)
	public static Object[] getSpeciesList(IContext<IBeeHousing> context, Object[] arg) {
		ISpeciesRoot beeRoot = getBeeRoot();
		IChromosomeType speciesType = beeRoot.getSpeciesChromosomeType();
		Collection<IAllele> allSpecies = AlleleManager.alleleRegistry.getRegisteredAlleles(speciesType);
		Map<Integer, Object> out = Maps.newHashMapWithExpectedSize(beeRoot.getSpeciesCount());

		int i = 0;
		for (IAllele allele : allSpecies) {
			if (((IAlleleSpecies) allele).isSecret()) continue;

			out.put(++i, MetaGenome.getAllele(allele));
		}

		return new Object[] { out };
	}

	@BasicObjectMethod.Inject(
			value = IBeeHousing.class, modId = Constants.MOD_ID, worldThread = false,
			doc = "function():table -- Get a list of all bee mutations"
	)
	public static Object[] getMutationsList(IContext<IBeeHousing> context, Object[] arg) {
		ISpeciesRoot beeRoot = getBeeRoot();
		List<? extends IMutation> mutations = beeRoot.getMutations(false);
		Map<Integer, Map<Object, Object>> out = Maps.newHashMapWithExpectedSize(mutations.size());

		int i = 0;
		for (IMutation mutation : mutations) {
			if (mutation.isSecret()) continue;

			out.put(++i, context.makePartialChild(mutation).getMeta());
		}

		return new Object[] { out };
	}
}
