package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * A method bound to a specific module
 */
public abstract class ModuleMethod extends BasicObjectMethod<IModule> {
	protected final ResourceLocation module;

	public ModuleMethod(String name, boolean worldThread, ResourceLocation module) {
		super(name, worldThread);
		this.module = module;
	}

	public ModuleMethod(String name, boolean worldThread, int priority, ResourceLocation module) {
		super(name, worldThread, priority);
		this.module = module;
	}

	@Override
	public boolean canApply(@Nonnull IContext<IModule> context) {
		return super.canApply(context) && context.getTarget().getModuleId().equals(module);
	}
}
