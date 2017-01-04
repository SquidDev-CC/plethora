package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * A module container which only contains one item.
 */
public class SingletonModuleContainer implements IModuleContainer {
	private final ResourceLocation thisModule;
	private final Set<ResourceLocation> modules;

	public SingletonModuleContainer(@Nonnull ResourceLocation module) {
		Preconditions.checkNotNull(module, "module cannot be null");
		this.thisModule = module;
		modules = Collections.singleton(module);
	}

	@Override
	public boolean hasModule(@Nonnull ResourceLocation module) {
		return thisModule.equals(module);
	}

	@Nonnull
	@Override
	public Set<ResourceLocation> getModules() {
		return modules;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SingletonModuleContainer that = (SingletonModuleContainer) o;

		return thisModule.equals(that.thisModule);
	}

	@Override
	public int hashCode() {
		return thisModule.hashCode();
	}
}
