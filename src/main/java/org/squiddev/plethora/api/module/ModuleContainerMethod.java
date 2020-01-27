package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * A method that requires a module to execute.
 */
public abstract class ModuleContainerMethod extends BasicMethod<IModuleContainer> {
	private final Set<ResourceLocation> modules;

	public ModuleContainerMethod(String name, Set<ResourceLocation> modules, int priority, String doc) {
		super(name, priority, doc);
		Preconditions.checkArgument(!modules.isEmpty(), "modules must be non-empty");
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
