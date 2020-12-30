package org.squiddev.plethora.core.executor;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IWorkMonitor;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
		attachmentName = access.getAttachmentName();
		this.runner = runner;
	}

	@Nullable
	@Override
	public Object[] execute(@Nonnull MethodResult result, @Nonnull ILuaContext context) throws LuaException, InterruptedException {
		assertAttached();
		if (result.isFinal()) return result.getResult();

		ComputerTask task = new ComputerTask(this, result.getCallback(), result.getResolver(), true);
		boolean ok = runner.submit(task);
		if (!ok) throw new LuaException("Task limit exceeded");

		while (true) {
			Object[] response = context.pullEvent(null);
			assertAttached();

			if (response.length >= 1 && EVENT_NAME.equals(response[0]) && task.isDone()) break;
		}

		if (task.error != null) throw task.error;
		return task.result;
	}

	@Override
	public void executeAsync(@Nonnull MethodResult result) throws LuaException {
		assertAttached();
		if (result.isFinal()) return;

		ComputerTask task = new ComputerTask(this, result.getCallback(), result.getResolver(), false);
		boolean ok = runner.submit(task);
		if (!ok) {
			task.cancel();
			throw new LuaException("Task limit exceeded");
		}
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

	private static class ComputerTask extends Task {
		private final IWorkMonitor monitor;
		private final ComputerAccessExecutor executor;
		private final boolean shouldQueue;

		ComputerTask(ComputerAccessExecutor executor, Callable<MethodResult> callback, MethodResult.Resolver resolver, boolean shouldQueue) {
			super(callback, resolver);
			this.executor = executor;
			this.shouldQueue = shouldQueue;
			monitor = executor.access.getMainThreadMonitor();
		}

		@Override
		void whenDone() {
			super.whenDone();
			if (!executor.attached || !shouldQueue) return;

			try {
				executor.access.queueEvent(EVENT_NAME, null);
			} catch (RuntimeException ignored) {
				// There is sadly nothing we can do about this, as there's always a slight
				// chance of a race condition.
			}
		}

		@Override
		boolean canWork() {
			return monitor == null || monitor.shouldWork();
		}

		@Override
		protected void submitTiming(long time) {
			super.submitTiming(time);
			if (monitor != null) monitor.trackWork(time, TimeUnit.NANOSECONDS);
		}

		@Override
		public boolean update() {
			if (!executor.attached) {
				cancel();
				return true;
			}

			return super.update();
		}
	}
}
