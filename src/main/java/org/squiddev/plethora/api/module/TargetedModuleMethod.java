package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * A module method that targets a separate class
 */
public abstract class TargetedModuleMethod<T> extends ModuleMethod {
	private final Class<T> klass;

	public TargetedModuleMethod(String name, ResourceLocation module, Class<T> klass) {
		super(name, module);
		this.klass = klass;
	}

	public TargetedModuleMethod(String name, int priority, ResourceLocation module, Class<T> klass) {
		super(name, priority, module);
		this.klass = klass;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.hasContext(klass);
	}
}
