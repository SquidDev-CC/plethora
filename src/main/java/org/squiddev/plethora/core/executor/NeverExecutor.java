package org.squiddev.plethora.core.executor;

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
public final class NeverExecutor implements IResultExecutor {
	public static final IResultExecutor INSTANCE = new NeverExecutor();

	private NeverExecutor() {
	}

	@Nullable
	@Override
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException {
		throw new LuaException("Cannot execute method");
	}

	@Override
	public void executeAsync(@Nonnull MethodResult result) throws LuaException {
		throw new LuaException("Cannot execute method");
	}
}
