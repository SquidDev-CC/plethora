package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;

/**
 * A method that requires a module to execute.
 */
public abstract class ModuleObjectMethod<T> extends BasicObjectMethod<T> implements IModuleMethod<T> {
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
	public boolean canApply(@Nonnull IPartialContext<T> context) {
		return super.canApply(context) && context.hasModule(module);
	}

	@Nonnull
	@Override
	public ResourceLocation getModule() {
		return module;
	}
}
