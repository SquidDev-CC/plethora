package org.squiddev.plethora.api.module;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * An item which holds a module
 */
public interface IModuleItem {
	/**
	 * Get the module from this item
	 *
	 * @param stack The stack to extract from
	 * @return The module.
	 */
	@Nonnull
	IModule getModule(@Nonnull ItemStack stack);

	/**
	 * Used to get additional context from a stack
	 *
	 * @param stack The stack to extract from
	 * @return The additional context items.
	 */
	@Nonnull
	Collection<IReference<?>> getAdditionalContext(@Nonnull ItemStack stack);
}
