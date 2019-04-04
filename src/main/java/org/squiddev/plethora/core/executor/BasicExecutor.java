package org.squiddev.plethora.core.executor;

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

	@Override
	public void executeAsync(@Nonnull MethodResult result) throws LuaException {
		if (result.isFinal()) return;

		Task task = new Task(result.getCallback(), result.getResolver());
		boolean ok = TaskRunner.SHARED.submit(task);
		if (!ok) {
			task.cancel();
			throw new LuaException("Task limit exceeded");
		}
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
				} catch (Exception | LinkageError | VirtualMachineError e) {
					PlethoraCore.LOG.error("Unexpected error", e);
					throw new LuaException("Java Exception Thrown: " + e);
				}
			}

			return null;
		}

		boolean done() {
			return resolver == null;
		}
	}
}
