package org.squiddev.plethora.core.executor;


import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Delaying version of {@link dan200.computercraft.core.computer.MainThread}.
 *
 * The implementation of this is a bit odd. It involves a linked list rather than
 * a normal ArrayList as we need to remove any item, whilst still allowing new tasks
 * to be added.
 */
public final class ContextDelayedExecutor extends AbstractDelayedExecutor implements IExecutorFactory {
	@Nonnull
	public IResultExecutor createExecutor(@Nullable final IComputerAccess access) {
		if (access == null) {
			return DefaultExecutor.INSTANCE;
		}

		return new IResultExecutor() {
			@Nullable
			@Override
			public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
				Preconditions.checkNotNull(result, "result cannot be null");

				if (result.isFinal()) {
					return result.getResult();
				} else {
					long id = nextId();

					SyncLuaTask task = new SyncLuaTask(access, result, id);
					if (addTask(task)) {
						return task.await(context);
					} else {
						throw new LuaException("Too many tasks");
					}
				}
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
		};
	}
}
