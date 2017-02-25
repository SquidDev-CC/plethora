package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrackingExecutor implements IExecutorFactory {
	private final IExecutorFactory parent;
	private boolean isAttached;

	public TrackingExecutor(IExecutorFactory parent) {
		this.parent = parent;
	}

	public void setAttached(boolean attached) {
		this.isAttached = attached;
	}

	@Nonnull
	@Override
	public IResultExecutor createExecutor(@Nullable IComputerAccess access) {
		final IResultExecutor executor = parent.createExecutor(access);
		return new IResultExecutor() {
			@Nullable
			@Override
			public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
				if (!isAttached) {
					throw new LuaException("The peripheral is no longer attached");
				}

				return executor.execute(result, context);
			}

			@Nonnull
			@Override
			public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
				if (!isAttached) {
					throw new LuaException("The peripheral is no longer attached");
				}

				return executor.executeAsync(result);
			}
		};
	}
}
