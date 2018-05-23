package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Executes a result after a set delay.
 */
public final class DefaultExecutor implements IResultExecutor, IExecutorFactory {
	public static final DefaultExecutor INSTANCE = new DefaultExecutor();

	private DefaultExecutor() {
	}

	@Override
	@Nullable
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
		if (result.isFinal()) {
			return result.getResult();
		} else {
			/**
			 * This is a horrible hack. Ideally we'd be able to have our own task manager. However
			 * {@link IComputerAccess#queueEvent(String, Object[])} requires being attached to a computer which we may
			 * no longer be. This results in this throwing an exception, so we never receive the event we are waiting
			 * for, resulting in the computer hanging until the terminate event is fired.
			 *
			 * To avoid this we queue the task each tick until the delay has elapsed. Most of the time this doesn't
			 * matter as there will be a zero tick delay.
			 */
			Task task = new Task(result);
			while (!task.done()) {
				context.executeMainThreadTask(task);
			}
			return task.returnValue;
		}
	}

	@Nonnull
	@Override
	public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
		return SharedDelayedExecutor.INSTANCE.executeAsync(result);
	}

	@Nonnull
	@Override
	public IResultExecutor createExecutor(@Nullable IComputerAccess access) {
		return INSTANCE;
	}

	private static class Task implements ILuaTask {
		public Object[] returnValue;
		private MethodResult.Resolver resolver;
		private Callable<MethodResult> callback;

		public Task(MethodResult result) {
			setup(result);
		}

		private void setup(MethodResult result) {
			if (result.isFinal()) {
				returnValue = result.getResult();
			} else {
				resolver = result.getResolver();
				callback = result.getCallback();
			}
		}

		@Override
		public Object[] execute() throws LuaException {
			if (resolver.update()) {
				resolver = null;

				try {
					setup(callback.call());
				} catch (LuaException e) {
					throw e;
				} catch (Throwable e) {
					DebugLogger.error("Unexpected error", e);
					throw new LuaException("Java Exception Thrown: " + e.toString());
				}
			}
			return null;
		}

		public boolean done() {
			return resolver == null;
		}
	}
}
