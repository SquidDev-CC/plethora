package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * A method bound to a specific module
 */
public abstract class ModuleMethod extends BasicMethod<IModule> implements IModuleMethod {
	protected final ResourceLocation module;

	public ModuleMethod(String name, ResourceLocation module) {
		this(name, module, 0, null);
	}

	public ModuleMethod(String name, ResourceLocation module, int priority) {
		this(name, module, priority, null);
	}

	public ModuleMethod(String name, ResourceLocation module, String doc) {
		this(name, module, 0, doc);
	}

	public ModuleMethod(String name, ResourceLocation module, int priority, String doc) {
		super(name, priority, doc);
		this.module = module;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.getTarget().getModuleId().equals(module);
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return module;
	}
}
