package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A result executor which should never be used.
 *
 * This is intended for cases where you will replace the executor before evaluating it (such as on peripherals).
 */
public class NeverExecutor implements IResultExecutor {
	public static final IResultExecutor INSTANCE = new NeverExecutor();

	private NeverExecutor() {
	}

	@Nullable
	@Override
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException {
		throw new LuaException("Cannot execute method");
	}

	@Nonnull
	@Override
	public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
		throw new LuaException("Cannot execute method");
	}
}
