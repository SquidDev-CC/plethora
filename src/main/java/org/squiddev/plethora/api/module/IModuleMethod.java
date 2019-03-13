package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IMethod;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A marker interface for methods which require one or more modules.
 *
 * This has no functionality: it is simply here for metadata collection
 */
public interface IModuleMethod<T> extends IMethod<T> {
	/**
	 * Get the modules that this method requires
	 *
	 * @return The modules that this method requires
	 */
	@Nonnull
	Collection<ResourceLocation> getModules();
}
