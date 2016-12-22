package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.ModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MethodsIntrospection {
	@ModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, worldThread = true,
		doc = "function():string -- Get this entity's UUID."
	)
	@Nullable
	public static Object[] getID(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		EntityLivingBase entity = context.getContext(EntityLivingBase.class);
		if (entity == null) throw new LuaException("Entity not found");

		return new Object[]{entity.getUniqueID().toString()};
	}

	@ModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, worldThread = true,
		doc = "function():string -- Get this entity's name"
	)
	public static Object[] getName(@Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		EntityLivingBase entity = context.getContext(EntityLivingBase.class);
		if (entity == null) throw new LuaException("Entity not found");

		return new Object[]{entity.getName()};
	}
}
