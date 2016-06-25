package org.squiddev.plethora.api.method;

import org.squiddev.plethora.api.reference.IReference;

/**
 * This holds the context for a method.
 *
 * This tracks the current object and all parent/associated objects
 */
public interface IContext<T> {
	/**
	 * Get the target for this context.
	 *
	 * @return The target for this context.
	 */
	T getTarget();

	/**
	 * Get surrounding context for an object.
	 * This is generally "parent" objects: an inventory might have a tile entity in its context.
	 * This does not include the target.
	 *
	 * @param klass The type of the klass to get.
	 * @return The context object or {@code null} if it doesn't exist.
	 * @see #hasContext(Class)
	 */
	<V> V getContext(Class<V> klass);

	/**
	 * Check if a context exists.
	 * This is generally "parent" objects: an inventory might have a tile entity in its context.
	 * This does not include the target.
	 *
	 * @param klass The type of the klass to get.
	 * @return If this context exists. It is more performant to check if {@link #getContext(Class)} returns null.
	 * @see #getContext(Class)
	 */
	<V> boolean hasContext(Class<V> klass);

	/**
	 * Make a child context
	 *
	 * @param target  The child's target
	 * @param context Additional context items
	 * @return The child context
	 */
	<U> IUnbakedContext<U> makeChild(IReference<U> target, IReference<?>... context);

	/**
	 * Make a child context
	 *
	 * @param target  The child's target
	 * @param context Additional context items
	 * @return The child context
	 */
	<U> IContext<U> makeBakedChild(U target, Object... context);

	/**
	 * Include additional properties in this context
	 *
	 * @param context The additional context items
	 * @return The new context
	 */
	IUnbakedContext<T> withContext(IReference<?>... context);
}
