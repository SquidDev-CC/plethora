package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * A method that requires a module to execute.
 */
public abstract class ModuleContainerObjectMethod extends BasicObjectMethod<IModuleContainer> implements IModuleMethod<IModuleContainer> {
	private final Set<ResourceLocation> modules;

	public ModuleContainerObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread) {
		this(name, modules, worldThread, 0, null);
	}

	public ModuleContainerObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, int priority) {
		this(name, modules, worldThread, priority, null);
	}

	public ModuleContainerObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, String docs) {
		this(name, modules, worldThread, 0, docs);
	}

	public ModuleContainerObjectMethod(String name, Set<ResourceLocation> modules, boolean worldThread, int priority, String docs) {
		super(name, worldThread, priority, docs);
		Preconditions.checkArgument(modules.size() > 0, "modules must be non-empty");
		this.modules = modules;
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IModuleContainer> context) {
		if (!super.canApply(context)) return false;

		IModuleContainer container = context.getTarget();
		for (ResourceLocation module : modules) {
			if (!container.hasModule(module)) return false;
		}

		return true;
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> getModules() {
		return modules;
	}
}
