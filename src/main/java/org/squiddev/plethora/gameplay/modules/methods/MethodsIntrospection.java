package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.EntityIdentifier;

import javax.annotation.Nonnull;

public final class MethodsIntrospection {
	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.class,
		doc = "function():string -- Get this entity's UUID."
	)
	public static Object[] getID(@Nonnull EntityIdentifier identifier, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) {
		return new Object[]{identifier.getId().toString()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.class,
		doc = "function():string -- Get this entity's UUID."
	)
	public static Object[] getName(@Nonnull EntityIdentifier identifier, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) {
		return new Object[]{identifier.getName()};
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.INTROSPECTION_S, target = EntityIdentifier.class,
		doc = "function():string -- Get this entity's UUID."
	)
	public static Object[] getMetaOwner(@Nonnull EntityIdentifier identifier, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		Entity entity = identifier.getEntity();
		return new Object[]{context.makePartialChild(entity).getMeta()};
	}
}
