package org.squiddev.plethora.api.module;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * A basic implementation of a module container.
 */
public class BasicModuleContainer implements IModuleContainer {
	public static final IModuleContainer EMPTY = new BasicModuleContainer(Collections.<ResourceLocation>emptySet());
	public static final IReference<IModuleContainer> EMPTY_REF = Reference.id(EMPTY);

	private final Set<ResourceLocation> modules;

	public BasicModuleContainer(@Nonnull Set<ResourceLocation> modules) {
		Preconditions.checkNotNull(modules, "modules cannot be null");
		this.modules = Collections.unmodifiableSet(modules);
	}

	@Override
	public boolean hasModule(@Nonnull ResourceLocation module) {
		return modules.contains(module);
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

		BasicModuleContainer that = (BasicModuleContainer) o;

		return modules.equals(that.modules);
	}

	@Override
	public int hashCode() {
		return modules.hashCode();
	}
}
