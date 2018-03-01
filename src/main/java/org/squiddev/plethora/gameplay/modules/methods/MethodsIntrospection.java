package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleContainerObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.utils.ContextHelpers.getOriginOr;

public final class MethodsIntrospection {
	@ModuleContainerObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, worldThread = false,
		doc = "function():string -- Get this entity's UUID."
	)
	public static Object[] getID(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Entity entity = getOriginOr(context, PlethoraModules.INTROSPECTION_S, Entity.class);
		if (entity == null) throw new LuaException("Entity not found");

		return new Object[]{entity.getUniqueID().toString()};
	}

	@ModuleContainerObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, worldThread = false,
		doc = "function():string -- Get this entity's name"
	)
	public static Object[] getName(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Entity entity = getOriginOr(context, PlethoraModules.INTROSPECTION_S, Entity.class);
		if (entity == null) throw new LuaException("Entity not found");

		return new Object[]{entity.getName()};
	}

	@ModuleContainerObjectMethod.Inject(
		module = {PlethoraModules.INTROSPECTION_S, PlethoraModules.SENSOR_S}, worldThread = true,
		doc = "function():string -- Get this entity's metadata."
	)
	public static Object[] getMetaOwner(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Entity entity = getOriginOr(context, PlethoraModules.INTROSPECTION_S, Entity.class);
		if (entity == null) throw new LuaException("Entity not found");

		return new Object[]{context.makePartialChild(entity).getMeta()};
	}
}
