package org.squiddev.plethora.api.module;

import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * An object representing a root level container for modules. This should be used
 * as a target for {@link org.squiddev.plethora.api.method.IMethod} when module methods
 * do not target a specific object.
 */
public interface IModuleContainer {

	/**
	 * Get a list of collection of all modules.
	 *
	 * You should avoid evaluating this directly: use {@link IPartialContext#getModules()}
	 * instead.
	 *
	 * @return A reference to the module collection. When evaluated this should return a constant value.
	 */
	@Nonnull
	IReference<Set<ResourceLocation>> getModules();
}
