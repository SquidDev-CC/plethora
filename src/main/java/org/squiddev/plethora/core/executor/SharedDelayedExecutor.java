package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SharedDelayedExecutor extends AbstractDelayedExecutor implements IResultExecutor {
	public static final SharedDelayedExecutor INSTANCE = new SharedDelayedExecutor();

	private SharedDelayedExecutor() {
	}

	@Nullable
	@Override
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
		return DefaultExecutor.INSTANCE.execute(result, context);
	}

	@Nonnull
	@Override
	public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
		if (result.isFinal()) {
			return Futures.immediateFuture(result.getResult());
		} else {
			AsyncLuaTask task = new AsyncLuaTask(result);
			if (addTask(task)) {
				return task.getFuture();
			} else {
				task.getFuture().cancel(true);
				throw new LuaException("Too many tasks");
			}
		}
	}
}
