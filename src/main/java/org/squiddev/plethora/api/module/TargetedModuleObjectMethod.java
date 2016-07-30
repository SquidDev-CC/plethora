package org.squiddev.plethora.api.module;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ISubTargetedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A module method that targets a separate class
 */
public abstract class TargetedModuleObjectMethod<T> extends ModuleObjectMethod implements ISubTargetedMethod<IModule, T> {
	private final Class<T> klass;

	public TargetedModuleObjectMethod(String name, ResourceLocation module, Class<T> klass, boolean worldThread) {
		this(name, module, klass, worldThread, 0, null);
	}

	public TargetedModuleObjectMethod(String name, ResourceLocation module, Class<T> klass, boolean worldThread, int priority) {
		this(name, module, klass, worldThread, priority, null);
	}

	public TargetedModuleObjectMethod(String name, ResourceLocation module, Class<T> klass, boolean worldThread, String docs) {
		this(name, module, klass, worldThread, 0, docs);
	}

	public TargetedModuleObjectMethod(String name, ResourceLocation module, Class<T> klass, boolean worldThread, int priority, String docs) {
		super(name, module, worldThread, priority, docs);
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


	@Nonnull
	@Override
	public Class<T> getSubTarget() {
		return klass;
	}
}
