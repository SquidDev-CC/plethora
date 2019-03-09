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
			return MethodResult.nextTick(() -> MethodResult.result(apply(context.bake(), args)));
		} else {
			return MethodResult.result(apply(context.safeBake(), args));
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
}
