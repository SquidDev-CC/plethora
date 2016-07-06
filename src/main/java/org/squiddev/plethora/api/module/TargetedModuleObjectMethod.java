package org.squiddev.plethora.api.module;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A module method that targets a separate class
 */
public abstract class TargetedModuleObjectMethod<T> extends ModuleObjectMethod {
	private final Class<T> klass;

	public TargetedModuleObjectMethod(String name, boolean worldThread, ResourceLocation module, Class<T> klass) {
		super(name, worldThread, module);
		this.klass = klass;
	}

	public TargetedModuleObjectMethod(String name, boolean worldThread, int priority, ResourceLocation module, Class<T> klass) {
		super(name, worldThread, priority, module);
		this.klass = klass;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.hasContext(klass);
	}

	@Nullable
	@Override
	public final Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
		return apply(context.getContext(klass), context, args);
	}

	@Nullable
	public abstract Object[] apply(@Nonnull T target, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException;

}
