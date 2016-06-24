package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.reference.IReference;

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
	IContext<T> bake() throws LuaException;

	/**
	 * Make a child context
	 *
	 * @param target  The child's target
	 * @param context Additional context items
	 * @return The child context
	 */
	<U> IUnbakedContext<U> makeChild(IReference<U> target, IReference<?>... context);

	/**
	 * Include additional properties in this context
	 *
	 * @param context The additional context items
	 * @return The new context
	 */
	IUnbakedContext<T> withContext(IReference<?>... context);
}
