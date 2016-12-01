package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.Set;

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
	 * Create a new context with a different cost handler but the same context
	 *
	 * @param handler The cost handler for this object
	 * @return The new context using the specified handlers
	 */
	IUnbakedContext<T> withCostHandler(@Nonnull ICostHandler handler);

	/**
	 * Create a new context with different modules but the same context
	 *
	 * @param modules A reference which will all modules for this context. This must return a constant value.
	 * @return The new context using the specified handlers
	 */
	IUnbakedContext<T> withModules(@Nonnull IReference<Set<ResourceLocation>> modules);


	/**
	 * Create a new context with a different executor the same context
	 *
	 * @param executor The result executor for this object
	 * @return The new context using the specified executor
	 */
	IUnbakedContext<T> withExecutor(@Nonnull IResultExecutor executor);

	/**
	 * Get a lua object from this context
	 *
	 * @return The built Lua object
	 * @throws IllegalStateException If the context cannot be baked
	 */
	@Nonnull
	ILuaObject getObject();

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
