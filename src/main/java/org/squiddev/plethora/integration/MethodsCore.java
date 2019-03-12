package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IMethodCollection;
import org.squiddev.plethora.api.method.wrapper.ArgumentTypes;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodsCore {
	@PlethoraMethod(doc = "-- Lists all modules available")
	public static Map<Integer, String> listModules(@FromTarget IModuleContainer container) {
		Map<Integer, String> modules = new HashMap<>();
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
	public static Object filterModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Set<ResourceLocation> oldModules = context.getTarget().getModules();
		Set<ResourceLocation> newModules = new HashSet<>();

		for (int i = 0; i < args.length; i++) {
			ResourceLocation module = ArgumentTypes.RESOURCE.get(args, i);
			if (oldModules.contains(module)) newModules.add(module);
		}

		if (newModules.size() == 0) return null;

		ILuaObject object = context
			.makeChildId(new BasicModuleContainer(newModules))
			.getObject();

		return object.getMethodNames().length == 0 ? null : object;
	}

	@PlethoraMethod(doc = "-- Get the documentation for all functions or the function specified. Errors if the function cannot be found.", worldThread = false)
	public static Object getDocs(@FromTarget IMethodCollection methodCollection, @Nullable String name) throws LuaException {
		if (name == null) {
			Map<String, String> out = new HashMap<>();
			for (IMethod method : methodCollection.methods()) {
				out.put(method.getName(), method.getDocString());
			}

			return out;
		} else {
			for (IMethod method : methodCollection.methods()) {
				if (method.getName().equals(name)) return method.getDocString();
			}

			throw new LuaException("No such method");
		}
	}
}
