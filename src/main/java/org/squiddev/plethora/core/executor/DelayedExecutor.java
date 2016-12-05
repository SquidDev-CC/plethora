package org.squiddev.plethora.core.executor;


import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.patcher.Logger;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Delaying version of {@link dan200.computercraft.core.computer.MainThread}.
 *
 * The implementation of this is a bit odd. It involves a linked list rather than
 * a normal ArrayList as we need to remove any item, whilst still allowing new tasks
 * to be added.
 */
public final class DelayedExecutor implements IExecutorFactory {
	private static final String EVENT_NAME = "plethora_task";

	private static final int MAX_TASKS_TOTAL = 5000;
	private static final int MAX_TASKS_TICK = 50;

	private int taskCount = 0;
	private LuaTask first;
	private LuaTask last;

	private final Object lock = new Object();
	private long lastTask = 0;

	private boolean addTask(LuaTask task) {
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

	private void cancel(long id) {
		synchronized (lock) {
			LuaTask previous = null;
			LuaTask task = first;
			while (task != null) {
				if (task.id == id) {
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
		first = null;
		last = null;
		taskCount = 0;
		lastTask = 0;
	}

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
					long id;
					synchronized (lock) {
						id = ++lastTask;
					}

					LuaTask task = new LuaTask(access, result, id);
					if (addTask(task)) {
						return task.await(context);
					} else {
						throw new LuaException("Too many tasks");
					}
				}
			}
		};
	}


	private final class LuaTask {
		// Linked list shenanigans
		private LuaTask next;

		// Resuming info
		private final long id;
		private final IComputerAccess access;

		private int remaining;
		private Callable<MethodResult> task;
		private Object[] result;

		private LuaTask(IComputerAccess access, MethodResult task, long id) {
			this.id = id;
			this.access = access;

			this.task = task.getCallback();
			remaining = task.getDelay();
		}

		private void yieldSuccess() {
			access.queueEvent(EVENT_NAME, new Object[]{id, true});
		}

		private void yieldFailure(String message) {
			access.queueEvent(EVENT_NAME, new Object[]{id, false, message});
		}

		private void update() {
			if (remaining < 0) {
				Logger.warn("Task has negative time remaining!");
				return;
			}

			if (remaining == 0) {
				remaining--;

				try {
					MethodResult next = task.call();
					if (next.isFinal()) {
						result = next.getResult();
						yieldSuccess();
					} else {
						task = next.getCallback();
						remaining = next.getDelay();
					}
				} catch (LuaException e) {
					yieldFailure(e.getMessage());
				} catch (Throwable e) {
					Logger.error("Error in task: ", e);
					yieldFailure("Java Exception Thrown: " + e.toString());
				}
			} else {
				remaining--;
			}
		}

		public boolean isDone() {
			return remaining < 0;
		}

		public Object[] await(ILuaContext context) throws LuaException, InterruptedException {
			Object[] response;
			try {
				do {
					response = context.pullEvent(EVENT_NAME);
				}
				while (response.length < 3 || !(response[1] instanceof Number) || !(response[2] instanceof Boolean) || (long) ((Number) response[1]).intValue() != id);
			} catch (InterruptedException e) {
				cancel(id);
				throw e;
			} catch (LuaException e) {
				cancel(id);
				throw e;
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
}
