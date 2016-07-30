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
	 * Get the doc string for this method.
	 *
	 * This can take several forms:
	 *
	 * - {@code Description of function}: A basic description with no types
	 * - {@code function(arg1:type [, optionArg:type]):returnType -- Description of function}: Function with return type
	 * - {@code function(arg1:type [, optionArg:type])->ret1:type [,optionRet1:type] -- Description of function}: Function with named return values
	 *
	 * Standard argument types are any, nil, string, number, integer, boolean and table.
	 *
	 * The function description can be multiple lines. The first line or sentence is read as a synopsis, with everything else being
	 * considered additional detail.
	 *
	 * @return The doc string. This can be {@code null} if you don't want to include one.
	 */
	@Nullable
	String getDocString();

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
	@Nonnull
	MethodResult apply(@Nonnull IUnbakedContext<T> context, @Nonnull Object[] args) throws LuaException;
}
