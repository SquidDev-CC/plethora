package org.squiddev.plethora.core.executor;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.ITickable;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Delaying version of {@link dan200.computercraft.core.computer.MainThread}.
 *
 * The implementation of this is a bit odd. It involves a linked list rather than
 * a normal ArrayList as we need to remove any item, whilst still allowing new tasks
 * to be added.
 */
public class AbstractDelayedExecutor implements ITickable {
	private static final String EVENT_NAME = "plethora_task";

	private static final int MAX_TASKS_TOTAL = 5000;
	private static final int MAX_TASKS_TICK = 50;

	private int taskCount = 0;
	private LuaTask first;
	private LuaTask last;

	private final Object lock = new Object();
	private long lastTask = 0;

	protected long nextId() {
		synchronized (lock) {
			return ++lastTask;
		}
	}

	protected boolean addTask(LuaTask task) {
		synchronized (lock) {
			if (taskCount < MAX_TASKS_TOTAL) {
				if (last != null) last.next = task;
				if (first == null) first = task;
				last = task;
				taskCount++;
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void update() {
		LuaTask previous = null;
		LuaTask task = first;
		int i = 0;

		while (task != null && i < MAX_TASKS_TICK) {
			task.update();
			if (task.isDone()) {
				synchronized (lock) {
					taskCount--;
					if (previous == null) {
						first = task.next;
					} else {
						previous.next = task.next;
					}
					if (task == last) last = null;
				}
			} else {
				previous = task;
			}

			synchronized (lock) {
				task = task.next;
			}

			i++;
		}
	}

	private void cancel(LuaTask search) {
		synchronized (lock) {
			LuaTask previous = null;
			LuaTask task = first;
			while (task != null) {
				if (task == search) {
					if (previous == null) {
						first = task.next;
					} else {
						previous.next = task.next;
					}
					taskCount--;
					return;
				}

				previous = task;
				task = task.next;
			}
		}
	}

	public void reset() {
		LuaTask task = first;
		while (task != null) {
			task.onCancel();
			task = task.next;
		}

		first = null;
		last = null;
		taskCount = 0;
		lastTask = 0;
	}

	protected abstract static class LuaTask {
		private LuaTask next;
		private MethodResult.Resolver resolver;
		private Callable<MethodResult> task;

		protected LuaTask(MethodResult task) {
			this.task = task.getCallback();
			resolver = task.getResolver();
		}

		protected abstract void onSuccess(Object[] result);

		protected abstract void onFailure(Throwable e);

		protected abstract void onCancel();

		private void update() {
			if (resolver.update()) {
				resolver = null;

				try {
					MethodResult next = task.call();
					if (next.isFinal()) {
						onSuccess(next.getResult());
					} else {
						task = next.getCallback();
						resolver = next.getResolver();
					}
				} catch (LuaException e) {
					onFailure(e);
				} catch (Throwable e) {
					DebugLogger.error("Error in task: ", e);
					onFailure(e);
				}
			}
		}

		public boolean isDone() {
			return resolver == null;
		}
	}


	protected final class SyncLuaTask extends LuaTask {
		// Resuming info
		private final IComputerAccess access;
		private Object[] result;
		private final long id;
		private Exception error = null;

		public SyncLuaTask(IComputerAccess access, MethodResult task, long id) {
			super(task);
			this.id = id;
			this.access = access;
		}

		@Override
		protected void onSuccess(Object[] result) {
			this.result = result;
			queueEvent(id, true);
		}

		@Override
		protected void onFailure(Throwable e) {
			String message = e instanceof LuaException
				? e.getMessage()
				: "Java Exception Thrown: " + e.toString();

			queueEvent(id, false, message);
		}

		private void queueEvent(Object... args) {
			try {
				access.queueEvent(EVENT_NAME, args);
			} catch (RuntimeException e) {
				error = e;
				DebugLogger.error("Cannot queue task result", e);
			}
		}

		@Override
		protected void onCancel() {
			queueEvent(id, false, "Task cancelled");
		}

		public Object[] await(ILuaContext context) throws LuaException, InterruptedException {
			Object[] response;
			try {
				do {
					response = context.pullEvent(EVENT_NAME);
				}
				while (error == null && (response.length < 3 || !(response[1] instanceof Number) || !(response[2] instanceof Boolean) || (long) ((Number) response[1]).intValue() != id));
			} catch (InterruptedException | LuaException e) {
				cancel(this);
				throw e;
			}

			if (error != null) {
				throw new LuaException(error.getMessage() == null
					? "Java Exception Thrown: " + error.toString()
					: error.getMessage());
			}

			if (!(Boolean) response[2]) {
				if (response.length >= 4 && response[3] instanceof String) {
					throw new LuaException((String) response[3]);
				} else {
					throw new LuaException();
				}
			} else {
				return result;
			}
		}
	}

	private final class DirectFuture<V> extends AbstractFuture<V> {
		@Override
		public boolean set(@Nullable V value) {
			return super.set(value);
		}

		@Override
		public boolean setException(Throwable throwable) {
			return super.setException(throwable);
		}
	}

	protected final class AsyncLuaTask extends LuaTask {
		private final DirectFuture<Object[]> future;

		public AsyncLuaTask(MethodResult task) {
			super(task);
			this.future = new DirectFuture<>();
		}

		@Override
		protected void onSuccess(Object[] result) {
			future.set(result);
		}

		@Override
		protected void onFailure(Throwable e) {
			future.setException(e);
		}

		@Override
		protected void onCancel() {
			future.cancel(true);
		}

		public ListenableFuture<Object[]> getFuture() {
			return future;
		}
	}
}
