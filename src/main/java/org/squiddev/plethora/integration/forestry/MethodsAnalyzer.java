package org.squiddev.plethora.integration.forestry;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ArgumentHelper;
import forestry.api.genetics.IAlleleSpecies;
import forestry.api.genetics.ISpeciesRoot;
import forestry.core.config.Constants;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleContainerObjectMethod;
import org.squiddev.plethora.utils.LuaList;

import java.util.Map;

import static forestry.api.genetics.AlleleManager.alleleRegistry;

public class MethodsAnalyzer {
	@ModuleContainerObjectMethod.Inject(
		module = IntegrationForestry.analyzerMod, worldThread = false, modId = Constants.MOD_ID,
		doc = "function():table -- Get a list of all species roots"
	)
	public static Object[] getSpeciesRoots(IContext<IModuleContainer> context, Object[] args) {
		return new Object[]{new LuaList<>(alleleRegistry.getSpeciesRoot().keySet()).asMap()};
	}

	private static ISpeciesRoot getSpeciesRoot(String uid) throws LuaException {
		ISpeciesRoot root = alleleRegistry.getSpeciesRoot(uid);
		if (root == null) throw new LuaException("Species root " + uid + " does not exist");
		return root;
	}

	@ModuleContainerObjectMethod.Inject(
		module = IntegrationForestry.analyzerMod, worldThread = false, modId = Constants.MOD_ID,
		doc = "function(root:string):table -- Get a list of all species in the given species root"
	)
	public static Object[] getSpeciesList(IContext<IModuleContainer> context, Object[] args) throws LuaException {
		String uid = ArgumentHelper.getString(args, 0);
		ISpeciesRoot root = getSpeciesRoot(uid);

		// please don't hurt me squid
		LuaList<Object> species = alleleRegistry.getRegisteredAlleles(root.getSpeciesChromosomeType()).stream()
			.map(IAlleleSpecies.class::cast)
			.filter(s -> !s.isSecret())
			.map(MetaGenome::getAlleleMeta)
			.collect(LuaList.toLuaList());

		return new Object[]{species.asMap()};
	}

	@ModuleContainerObjectMethod.Inject(
		module = IntegrationForestry.analyzerMod, worldThread = false, modId = Constants.MOD_ID,
		doc = "function(root:string):table -- Get a list of all mutations in the given species root"
	)
	public static Object[] getMutationsList(IContext<IModuleContainer> context, Object[] args) throws LuaException {
		String uid = ArgumentHelper.getString(args, 0);
		ISpeciesRoot root = getSpeciesRoot(uid);

		LuaList<Map<Object, Object>> mutations = root.getMutations(false).stream()
			.filter(s -> !s.isSecret())
			.map(m -> context.makePartialChild(m).getMeta())
			.collect(LuaList.toLuaList());

		return new Object[]{mutations.asMap()};
	}
}
