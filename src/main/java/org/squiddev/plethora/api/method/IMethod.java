package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Lua side method targeting a class.
 *
 * There are several ways of using {@link IMethod}:
 * - Extend {@link BasicMethod}, one of its subclasses or write your own. Then register using {@link Inject}.
 * - As above but register using {@link IMethodRegistry#registerMethod(Class, IMethod)}.
 * - Create a static method on a class and register using {@link BasicMethod.Inject} or similar. If you wish to
 * use a custom base class see {@link IMethodBuilder}.
 *
 * @see BasicMethod
 * @see IMethodRegistry#registerMethod(Class, IMethod)
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
	boolean canApply(@Nonnull IPartialContext<T> context);

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

	/**
	 * Determine whether this method will ever yield.
	 *
	 * @return If this method will yield. Only return {@code false} if you are 100% certain it will never yield.
	 */
	boolean willYield();

	/**
	 * Automatically register a method.
	 *
	 * The class must have a public constructor and implement {@link IMethod}.
	 *
	 * @see IMethodRegistry#registerMethod(Class, IMethod)
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.CLASS)
	@interface Inject {
		/**
		 * The target class
		 *
		 * @return The target class
		 */
		Class<?> value();

		/**
		 * Set if this method depends on a mod
		 *
		 * @return The mod's id
		 * @see net.minecraftforge.fml.common.Optional.Method
		 * @see net.minecraftforge.fml.common.Optional.Interface
		 */
		String modId() default "";
	}
}
