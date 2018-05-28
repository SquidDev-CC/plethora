package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import static dan200.computercraft.core.apis.ArgumentHelper.getString;

public class MethodsModules {
	@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function():table -- Lists all modules available"
	)
	public static Object[] listModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Map<Integer, String> modules = Maps.newHashMap();
		int i = 0;
		for (ResourceLocation module : context.getTarget().getModules()) {
			modules.put(++i, module.toString());
		}
		return new Object[]{modules};
	}

	@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function(name:string):boolean -- Checks whether a module is available"
	)
	public static Object[] hasModule(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		ResourceLocation module = new ResourceLocation(getString(args, 0));
		return new Object[]{context.getTarget().hasModule(module)};
	}

	@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function(names:string...):table|nil -- Gets the methods which require these modules"
	)
	public static Object[] filterModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Set<ResourceLocation> oldModules = context.getTarget().getModules();
		Set<ResourceLocation> newModules = Sets.newHashSet();

		for (int i = 0; i < args.length; i++) {
			ResourceLocation module = new ResourceLocation(getString(args, i));
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
