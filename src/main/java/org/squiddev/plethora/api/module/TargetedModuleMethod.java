package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ISubTargetedMethod;

import javax.annotation.Nonnull;

/**
 * A module method that targets a separate class
 */
public abstract class TargetedModuleMethod<T> extends ModuleMethod implements ISubTargetedMethod<IModule, T> {
	private final Class<T> klass;

	public TargetedModuleMethod(String name, ResourceLocation module, Class<T> klass) {
		this(name, module, klass, 0, null);
	}

	public TargetedModuleMethod(String name, ResourceLocation module, Class<T> klass, int priority) {
		this(name, module, klass, priority, null);
	}

	public TargetedModuleMethod(String name, ResourceLocation module, Class<T> klass, String docs) {
		this(name, module, klass, 0, docs);
	}

	public TargetedModuleMethod(String name, ResourceLocation module, Class<T> klass, int priority, String docs) {
		super(name, module, priority, docs);
		this.klass = klass;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.hasContext(klass);
	}

	@Nonnull
	@Override
	public Class<T> getSubTarget() {
		return klass;
	}
}
