package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * A context whose references haven't been resolved
 */
public interface IUnbakedContext<T> {
	/**
	 * Bake a context, ensuring all references are valid
	 *
	 * Note, this method is NOT thread safe and MUST be called from the server thread. Use {@link #safeBake()} if
	 * you need a safe version.
	 *
	 * @return The baked context
	 * @throws LuaException If
	 * @see IReference#get()
	 */
	@Nonnull
	IContext<T> bake() throws LuaException;

	/**
	 * Bake a context, ensuring all references are valid.
	 *
	 * This method is thread safe, though the result object may not be safe to use on any thread.
	 *
	 * @return The baked context
	 * @throws LuaException If
	 * @see IReference#safeGet()
	 */
	@Nonnull
	IContext<T> safeBake() throws LuaException;

	/**
	 * Get the cost handler associated with this object
	 *
	 * @return The parent's cost handler
	 */
	@Nonnull
	ICostHandler getCostHandler();

	/**
	 * Get the result executor for this context
	 *
	 * @return The context's result executor
	 */
	@Nonnull
	IResultExecutor getExecutor();
}
