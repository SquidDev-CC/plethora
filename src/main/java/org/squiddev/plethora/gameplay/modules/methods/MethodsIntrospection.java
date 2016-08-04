package org.squiddev.plethora.gameplay.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.ModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MethodsIntrospection {
	@IMethod.Inject(IModule.class)
	public static final class MethodEntityGetID extends ModuleObjectMethod {
		public MethodEntityGetID() {
			super("getID", PlethoraModules.INTROSPECTION, true, "function():string -- Get this entity's UUID.");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityLivingBase entity = context.getContext(EntityLivingBase.class);
			if (entity == null) throw new LuaException("Entity not found");

			return new Object[]{entity.getUniqueID().toString()};
		}
	}

	@IMethod.Inject(IModule.class)
	public static final class MethodEntityGetName extends ModuleObjectMethod {
		public MethodEntityGetName() {
			super("getName", PlethoraModules.INTROSPECTION, true, "function():string -- Get this entity's name");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			EntityLivingBase entity = context.getContext(EntityLivingBase.class);
			if (entity == null) throw new LuaException("Entity not found");

			return new Object[]{entity.getName()};
		}
	}
}
