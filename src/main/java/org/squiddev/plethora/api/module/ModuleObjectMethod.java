package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * A method bound to a specific module
 */
public abstract class ModuleObjectMethod extends BasicObjectMethod<IModule> implements IModuleMethod {
	protected final ResourceLocation module;

	public ModuleObjectMethod(String name, ResourceLocation module, boolean worldThread) {
		this(name, module, worldThread, 0, null);
	}

	public ModuleObjectMethod(String name, ResourceLocation module, boolean worldThread, int priority) {
		this(name, module, worldThread, priority, null);
	}

	public ModuleObjectMethod(String name, ResourceLocation module, boolean worldThread, String docs) {
		this(name, module, worldThread, 0, docs);
	}

	public ModuleObjectMethod(String name, ResourceLocation module, boolean worldThread, int priority, String docs) {
		super(name, worldThread, priority, docs);
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
