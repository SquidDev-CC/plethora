package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IMethod;

import javax.annotation.Nonnull;

/**
 * A marker interface for methods which target a specific module.
 *
 * This has no functionality: it is simply here for metadata collection
 */
public interface IModuleMethod<T> extends IMethod<T> {
	/**
	 * Get the module that this method targets
	 *
	 * @return The module that this method targets
	 */
	@Nonnull
	ResourceLocation getModule();
}
