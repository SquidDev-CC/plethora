package org.squiddev.plethora.api.method;

import javax.annotation.Nullable;

/**
 * A method which targets a child property.
 *
 * For instance objects which reference particular {@link net.minecraft.item.Item} classes
 * will target {@link net.minecraft.item.ItemStack} instead.
 *
 * This has no functionality: it is simply here for metadata collection
 */
public interface ISubTargetedMethod<T, U> extends IMethod<T> {
	/**
	 * Get the sub-target for this method
	 *
	 * @return The method's sub-target, or {@code null} if we have no sub-target.
	 */
	@Nullable
	Class<U> getSubTarget();
}
