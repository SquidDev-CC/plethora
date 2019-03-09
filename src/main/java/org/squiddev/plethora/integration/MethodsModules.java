package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.gen.ArgumentTypes;
import org.squiddev.plethora.api.method.gen.FromTarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class MethodsModules {
	@PlethoraMethod(doc = "-- Lists all modules available")
	public static Map<Integer, String> listModules(@FromTarget IModuleContainer container) {
		Map<Integer, String> modules = Maps.newHashMap();
		int i = 0;
		for (ResourceLocation module : container.getModules()) {
			modules.put(++i, module.toString());
		}
		return modules;
	}

	@PlethoraMethod(doc = "-- Checks whether a module is available")
	public static boolean hasModule(@FromTarget IModuleContainer container, @Nonnull ResourceLocation module) {
		return container.hasModule(module);
	}

	@PlethoraMethod(doc = "function(names:string...):table|nil -- Gets the methods which require these modules")
	public static Object[] filterModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Set<ResourceLocation> oldModules = context.getTarget().getModules();
		Set<ResourceLocation> newModules = Sets.newHashSet();

		for (int i = 0; i < args.length; i++) {
			ResourceLocation module = ArgumentTypes.RESOURCE.get(args, i);
			if (oldModules.contains(module)) newModules.add(module);
		}

		if (newModules.size() == 0) return null;

		ILuaObject object = context
			.makeChildId(new BasicModuleContainer(newModules))
			.getObject();

		if (object.getMethodNames().length == 0) {
			return null;
		} else {
			return new Object[]{object};
		}
	}
}
