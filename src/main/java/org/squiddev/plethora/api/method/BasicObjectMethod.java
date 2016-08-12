package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

/**
 * Basic wrapper for methods which deals returns an object array.
 */
public abstract class BasicObjectMethod<T> extends BasicMethod<T> {
	private final boolean worldThread;

	public BasicObjectMethod(String name, boolean worldThread) {
		this(name, worldThread, 0, null);
	}

	public BasicObjectMethod(String name, boolean worldThread, String docs) {
		this(name, worldThread, 0, docs);
	}

	public BasicObjectMethod(String name, boolean worldThread, int priority) {
		this(name, worldThread, priority, null);
	}

	public BasicObjectMethod(String name, boolean worldThread, int priority, String docs) {
		super(name, priority, docs);
		this.worldThread = worldThread;
	}

	@Nonnull
	@Override
	public final MethodResult apply(@Nonnull final IUnbakedContext<T> context, @Nonnull final Object[] args) throws LuaException {
		if (worldThread) {
			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					return MethodResult.result(apply(context.bake(), args));
				}
			});
		} else {
			return MethodResult.result(apply(context.bake(), args));
		}
	}

	/**
	 * Apply the method
	 *
	 * @param context The context to apply within
	 * @param args    The arguments this function was called with
	 * @return The return values
	 * @throws LuaException     On the event of an error
	 * @throws RuntimeException Unhandled errors: these will be rethrown as {@link LuaException}s and the call stack logged.
	 * @see dan200.computercraft.api.lua.ILuaObject#callMethod(ILuaContext, int, Object[])
	 * @see IMethod#apply(IUnbakedContext, Object[])
	 */
	@Nullable
	public abstract Object[] apply(@Nonnull IContext<T> context, @Nonnull Object[] args) throws LuaException;

	/**
	 * Delegate to a normal method from a {@link BasicObjectMethod}.
	 *
	 * The method should be a public and static with the same signature as {@link BasicObjectMethod#apply(IContext, Object[])}.
	 * This does not allow fine grain control over whether a method can be applied or not. If you require
	 * {@link IMethod#canApply(IContext)} you should use a normal {@link IMethod} instead.
	 *
	 * Use {@link net.minecraftforge.fml.common.Optional.Method} if you require a mod to be loaded.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Inject {
		/**
		 * The name this function should be exposed as.
		 *
		 * This defaults to the method's name
		 *
		 * @return The function's name
		 * @see IMethod#getName()
		 */
		String name() default "";

		/**
		 * The class this method targets.
		 *
		 * @return The target class.
		 */
		Class<?> value();

		/**
		 * The priority of the method.
		 *
		 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Methods
		 * with higher priorities will be preferred.
		 *
		 * @return The method's priority
		 * @see IMethod#getPriority()
		 */
		int priority() default 0;

		/**
		 * The method's doc string.
		 *
		 * See {@link IMethod#getDocString()} for format information
		 *
		 * @return The method's doc string
		 * @see IMethod#getDocString()
		 */
		String doc() default "";

		/**
		 * Run this method on the world thread
		 *
		 * @return Whether this method should be run on the world thread
		 * @see BasicObjectMethod#BasicObjectMethod(String, boolean)
		 */
		boolean worldThread();

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
