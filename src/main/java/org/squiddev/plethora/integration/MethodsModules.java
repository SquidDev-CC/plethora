package org.squiddev.plethora.integration;

import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;
import java.util.Set;

import static org.squiddev.plethora.api.method.ArgumentHelper.getString;

public class MethodsModules {
	@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function():table -- Lists all modules available"
	)
	public static Object[] listModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		return new Object[]{context.getModules().toArray()};
	}

	@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function():boolean -- Checks whether a module is available"
	)
	public static Object[] hasModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		ResourceLocation module = new ResourceLocation(getString(args, 0));
		return new Object[]{context.hasModule(module)};
	}

	/*@BasicObjectMethod.Inject(
		value = IModuleContainer.class, worldThread = true,
		doc = "function(names:string...):table -- Gets the methods which require these modules"
	)*/
	public static Object[] filterModules(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final ResourceLocation[] modules = new ResourceLocation[args.length];
		for (int i = 0; i < args.length; i++) {
			modules[i] = new ResourceLocation(getString(args, i));
		}

		final IModuleContainer container = context.getTarget();
		IModuleContainer filteredContainer = new IModuleContainer() {
			@Nonnull
			@Override
			public Set<ResourceLocation> get() throws LuaException {
				Set<ResourceLocation> existing = container.get();
				Set<ResourceLocation> out = Sets.newHashSetWithExpectedSize(modules.length);
				for (ResourceLocation module : modules) {
					if (existing.contains(module)) out.add(module);
				}
				return out;
			}
		};

		// FIXME: This doesn't actually work, we need to pass it to the primary module reference too.
		IUnbakedContext<IModuleContainer> selected = context.makeChild(Reference.id(filteredContainer));

		return new Object[]{selected.getObject()};
	}
}
