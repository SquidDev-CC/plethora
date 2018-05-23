package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * A result executor which attempts to run through {@link IComputerAccess#queueEvent(String, Object[])}
 *
 * This attempts to ensure you're still attached to the computer every tick. If you are not, it'll cancel all tasks
 * and throw an exception.
 *
 * Note that this should not be vulnerable to blocking after a peripheral detaches, as we do not filter our events and
 * so should receive {@code peripheral_detach} events (allowing us to detect the detach and so error).
 */
public class ComputerAccessExecutor implements IResultExecutor {
	private static final String EVENT_NAME = "plethora_task";

	private final IComputerAccess access;
	private final String attachmentName;
	private final TaskRunner runner;

	private volatile boolean attached;

	public ComputerAccessExecutor(IComputerAccess access, TaskRunner runner) {
		this.access = access;
		this.attachmentName = access.getAttachmentName();
		this.runner = runner;
	}

	@Nullable
	@Override
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
		assertAttached();
		if (result.isFinal()) return result.getResult();

		BlockingTask task = new BlockingTask(result.getCallback(), result.getResolver());
		boolean ok = runner.submit(task);
		if (!ok) throw new LuaException("Task limit exceeded");

		while (true) {
			Object[] response = context.pullEvent(null);
			assertAttached();

			if (response.length >= 1 && EVENT_NAME.equals(response[0]) && task.finished()) break;
		}

		if (task.error != null) throw task.error;
		return task.result;
	}

	@Nonnull
	@Override
	public ListenableFuture<Object[]> executeAsync(@Nonnull MethodResult result) throws LuaException {
		assertAttached();
		if (result.isFinal()) return Futures.immediateFuture(result.getResult());

		FutureTask task = new FutureTask(result.getCallback(), result.getResolver());
		boolean ok = runner.submit(task);
		if (!ok) {
			task.getFuture().cancel(true);
			throw new LuaException("Task limit exceeded");
		}

		return task.getFuture();
	}

	private void assertAttached() throws LuaException {
		if (!attached) throw new LuaException("Peripheral '" + attachmentName + "' is no longer attached");
	}

	public void attach() {
		attached = true;
	}

	public void detach() {
		attached = false;
	}

	private class BlockingTask extends Task {
		Object[] result;
		LuaException error;

		BlockingTask(Callable<MethodResult> callback, MethodResult.Resolver resolver) {
			super(callback, resolver);
		}

		@Override
		protected void finish(Object[] result) {
			this.result = result;
			try {
				access.queueEvent(EVENT_NAME, null);
			} catch (RuntimeException e) {
				DebugLogger.error("Cannot queue event. This is an unavoidable race condition. Sorry.", e);
			}
		}

		@Override
		protected void finish(@Nonnull LuaException e) {
			this.error = e;
		}

		@Override
		public boolean update() {
			if (!attached) {
				markFinished();
				return true;
			}

			return super.update();
		}
	}

	private class FutureTask extends org.squiddev.plethora.core.executor.FutureTask {
		FutureTask(Callable<MethodResult> callback, MethodResult.Resolver resolver) {
			super(callback, resolver);
		}

		@Override
		public boolean update() {
			if (!attached) {
				markFinished();
				getFuture().cancel(true);
				return true;
			}

			return super.update();
		}
	}
}
