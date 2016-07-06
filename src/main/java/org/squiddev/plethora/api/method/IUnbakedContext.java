package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;
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
	 * @return The baked context
	 * @throws LuaException If
	 * @see IReference#get()
	 */
	@Nonnull
	IContext<T> bake() throws LuaException;

	/**
	 * Make a child context
	 *
	 * @param target  The child's target
	 * @param context Additional context items
	 * @return The child context
	 */
	@Nonnull
	<U> IUnbakedContext<U> makeChild(@Nonnull IReference<U> target, @Nonnull IReference<?>... context);

	/**
	 * Include additional properties in this context
	 *
	 * @param context The additional context items
	 * @return The new context
	 */
	@Nonnull
	IUnbakedContext<T> withContext(@Nonnull IReference<?>... context);

	/**
	 * Get a lua object from this context
	 *
	 * @return The built Lua object
	 * @throws IllegalStateException If the context cannot be baked
	 */
	@Nonnull
	ILuaObject getObject();
}
