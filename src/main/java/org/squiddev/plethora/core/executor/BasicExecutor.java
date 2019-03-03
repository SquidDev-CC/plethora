package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * A result executor which relies on {@link ILuaContext#executeMainThreadTask(ILuaTask)} in order to execute tasks.
 *
 * This queues a task every tick until all results in the chain have been resolved and evaluated. This could end up
 * being rather spamming in terms of events, but does not usually become an issue as most tasks finish within the tick.
 */
public final class BasicExecutor implements IResultExecutor {
	public static final BasicExecutor INSTANCE = new BasicExecutor();

	private BasicExecutor() {
	}

	@Override
	@Nullable
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
		if (result.isFinal()) return result.getResult();

		BlockingTask task = new BlockingTask(result.getResolver(), result.getCallback());
		while (!task.done()) {
			context.executeMainThreadTask(task);
		}
		return task.returnValue;
	}

	@Nonnull
	@Override
	public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
		if (result.isFinal()) return Futures.immediateFuture(result.getResult());

		FutureTask task = new FutureTask(result.getCallback(), result.getResolver());
		boolean ok = TaskRunner.SHARED.submit(task);
		if (!ok) {
			task.getFuture().cancel(true);
			throw new LuaException("Task limit exceeded");
		}

		return task.getFuture();
	}

	private static class BlockingTask implements ILuaTask {
		Object[] returnValue;
		private MethodResult.Resolver resolver;
		private Callable<MethodResult> callback;

		BlockingTask(MethodResult.Resolver resolver, Callable<MethodResult> callback) {
			this.resolver = resolver;
			this.callback = callback;
		}

		@Override
		public Object[] execute() throws LuaException {
			while (resolver.update()) {
				resolver = null;

				try {
					MethodResult result = callback.call();
					if (result.isFinal()) {
						returnValue = result.getResult();
						return null;
					} else {
						resolver = result.getResolver();
						callback = result.getCallback();
					}
				} catch (LuaException e) {
					throw e;
				} catch (Throwable e) {
					PlethoraCore.LOG.error("Unexpected error", e);
					throw new LuaException("Java Exception Thrown: " + e.toString());
				}
			}

			return null;
		}

		boolean done() {
			return resolver == null;
		}
	}
}
