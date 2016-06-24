package org.squiddev.plethora.api.method;

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
	 * Get an environment object.
	 * This is something which only exists at method call time: such as {@link dan200.computercraft.api.lua.ILuaContext}.
	 * Use {@link #hasEnvironment(Class)} when checking to apply.
	 *
	 * @param klass The type of the klass to get.
	 * @return The environment object or {@code null} if it doesn't exist.
	 * @see #hasEnvironment(Class)
	 */
	<V> V getEnvironment(Class<V> klass);

	/**
	 * Get an environment object.
	 * This is something which only exists at method call time: such as {@link dan200.computercraft.api.lua.ILuaContext}.
	 * This method checks if it will exist at call time.
	 *
	 * @param klass The type of the klass to get.
	 * @return If this object will exist at call time.
	 * @see #getEnvironment(Class)
	 */
	<V> boolean hasEnvironment(Class<V> klass);

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
}
