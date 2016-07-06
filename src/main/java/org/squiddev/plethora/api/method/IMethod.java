package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Lua side method targeting a class
 * Register with {@link IMethodRegistry}
 */
public interface IMethod<T> {
	/**
	 * The name of this method
	 *
	 * @return The name of this method
	 */
	@Nonnull
	String getName();

	/**
	 * If this function should be executed on the world thread
	 *
	 * @return If this function should be executed on the world thread.
	 */
	boolean worldThread();

	/**
	 * Get the priority of this provider
	 *
	 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Providers
	 * with higher priorities will be preferred.
	 *
	 * @return The provider's priority
	 */
	int getPriority();

	/**
	 * Check if this function can be applied in the given context.
	 *
	 * @param context The context to check in
	 * @return If this function can be applied.
	 * @see IContext#hasContext(Class)
	 */
	boolean canApply(@Nonnull IContext<T> context);

	/**
	 * Apply the method
	 *
	 * @param context The context to apply within
	 * @param args    The arguments this function was called with
	 * @return The return values
	 * @throws LuaException     On the event of an error
	 * @throws RuntimeException Unhandled errors: these will be rethrown as {@link LuaException}s and the call stack logged.
	 * @see dan200.computercraft.api.lua.ILuaObject#callMethod(ILuaContext, int, Object[])
	 */
	@Nullable
	Object[] apply(@Nonnull IContext<T> context, @Nonnull Object[] args) throws LuaException;
}
