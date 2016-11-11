package org.squiddev.plethora.api.method;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * A context for a method call
 */
public interface IPartialContext<T> {
	/**
	 * Get the target for this context.
	 *
	 * @return The target for this context.
	 */
	@Nonnull
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
	<V> V getContext(@Nonnull Class<V> klass);

	/**
	 * Check if a context exists.
	 * This is generally "parent" objects: an inventory might have a tile entity in its context.
	 * This does not include the target.
	 *
	 * @param klass The type of the klass to get.
	 * @return If this context exists. It is more performant to check if {@link #getContext(Class)} returns null.
	 * @see #getContext(Class)
	 */
	<V> boolean hasContext(@Nonnull Class<V> klass);

	/**
	 * Make a child context
	 *
	 * @param target  The child's target
	 * @param context Additional context items
	 * @return The child context
	 */
	@Nonnull
	<U> IPartialContext<U> makePartialChild(@Nonnull U target, @Nonnull Object... context);

	/**
	 * Get the cost handler associated with this object.
	 *
	 * @return The cost handler
	 */
	@Nonnull
	ICostHandler getCostHandler();

	/**
	 * @param key The lookup for transfer location
	 * @return The valid transfer location or {@code null} if none exists.
	 */
	@Nullable
	Object getTransferLocation(@Nonnull String key);

	/**
	 * Get all primary transfer locations
	 *
	 * @return All valid locations
	 */
	@Nonnull
	Set<String> getTransferLocations();

	/**
	 * Check whether this context includes a given module.
	 *
	 * This will not change over the course of the context's lifetime.
	 *
	 * @param module The module to check
	 * @return Whether this context has a the specified module
	 */
	boolean hasModule(@Nonnull ResourceLocation module);

	/**
	 * Get a collection of all modules in this context.
	 *
	 * This will not change over the course of the context's lifetime.
	 *
	 * @return A read-only set of all available modules.
	 */
	@Nonnull
	Set<ResourceLocation> getModules();

	/**
	 * Get the metadata for this object
	 *
	 * @return The object's metadata
	 * @see org.squiddev.plethora.api.meta.IMetaRegistry#getMeta(IPartialContext)
	 */
	@Nonnull
	Map<Object, Object> getMeta();
}
