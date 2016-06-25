package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * A method bound to a specific module
 */
public abstract class ModuleMethod<T> extends BasicMethod<T> {
	protected final ResourceLocation module;

	protected ModuleMethod(String name, boolean worldThread, ResourceLocation module) {
		super(name, worldThread);
		this.module = module;
	}

	public ModuleMethod(String name, ResourceLocation module) {
		super(name);
		this.module = module;
	}

	@Override
	public boolean canApply(@Nonnull IContext<T> context) {
		if (!super.canApply(context)) return false;
		IModule module = context.getContext(IModule.class);
		return module != null && module.getModuleId().equals(module);
	}
}
