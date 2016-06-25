package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

/**
 * Modules can be used to gate specific methods.
 *
 * A module item ({@link IModuleItem}) can be placed in neural networks or manipulators to allow access to
 * other methods. {@link IModule} will be placed in {@link org.squiddev.plethora.api.method.IMethod#canApply(IContext)}
 * so you can require it there. You can alternatively use {@link ModuleMethod}.
 *
 * @see IModuleItem
 * @see ModuleMethod
 */
public interface IModule {
	/**
	 * Get the ID for this module
	 *
	 * @return The module for this ID.
	 */
	@Nonnull
	ResourceLocation getModuleId();
}
