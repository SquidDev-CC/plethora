package org.squiddev.plethora.core;


import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.Callable;

/**
 * Delaying version of {@link dan200.computercraft.core.computer.MainThread}.
 *
 * The implementation of this is a bit odd. It involves a linked list rather than
 * a normal ArrayList as we need to remove any item, whilst still allowing new tasks
 * to be added.
 */
public class TaskHandler {
	private static final String EVENT_NAME = "plethora_task";

	private static final int MAX_TASKS_TOTAL = 50000;
	private static final int MAX_TASKS_TICK = 1000;

	private static LinkedHashSet<LuaTask> tasks = new LinkedHashSet<LuaTask>();

	private static final Object lock = new Object();
	private static long lastTask = 0;

	private static long getNextId() {
		synchronized (lock) {
			return ++lastTask;
		}
	}

	public static void reset() {
		tasks.clear();
		lastTask = 0;
	}

	private static boolean addTask(LuaTask task) {
		synchronized (lock) {
			if (tasks.size() < MAX_TASKS_TOTAL) {
				tasks.add(task);
				return true;
			} else {
				return false;
			}
		}
	}

	public static Object[] addTask(IComputerAccess access, ILuaContext context, Callable<MethodResult> callback, int delay) throws LuaException, InterruptedException {
		Preconditions.checkNotNull(access, "access cannot be null");
		Preconditions.checkNotNull(context, "context cannot be null");
		Preconditions.checkNotNull(callback, "callback cannot be null");
		Preconditions.checkArgument(delay >= 0, "delay must be >= 0");

		long id = getNextId();
		LuaTask task = new LuaTask(access, callback, delay, id);
		if (!addTask(task)) throw new LuaException("Too many tasks");

		Object[] response;
		try {
			do {
				response = context.pullEvent(EVENT_NAME);
			}
			while (response.length < 3 || !(response[1] instanceof Number) || !(response[2] instanceof Boolean) || (long) ((Number) response[1]).intValue() != id);
		} catch (InterruptedException e) {
			cancel(task);
			throw e;
		} catch (LuaException e) {
			cancel(task);
			throw e;
		}

		if (!(Boolean) response[2]) {
			if (response.length >= 4 && response[3] instanceof String) {
				throw new LuaException((String) response[3]);
			} else {
				throw new LuaException();
			}
		} else {
			return task.returnValues;
		}
	}

	public static void update() {
		int executed = 0;
		synchronized (lock) {
			Iterator<LuaTask> iterator = tasks.iterator();

			while (iterator.hasNext()) {
				LuaTask task = iterator.next();
				if (task.remaining == 0) {
					if (++executed <= MAX_TASKS_TICK && task.execute()) {
						iterator.remove();
					}
				} else {
					task.remaining--;
				}
			}
		}
	}

	private static void cancel(LuaTask task) {
		synchronized (lock) {
			tasks.remove(task);
		}
	}

	private static final class LuaTask {
		public int remaining;

		private final IComputerAccess access;
		private final long id;

		private Callable<MethodResult> task;

		private Object[] returnValues;

		private LuaTask(IComputerAccess access, Callable<MethodResult> task, int delay, long id) {
			this.id = id;
			this.access = access;

			this.task = task;
			remaining = delay;
		}

		private void yieldFailure(String message) {
			access.queueEvent(EVENT_NAME, new Object[]{id, false, message});
		}

		public boolean execute() {
			try {
				MethodResult result = task.call();
				if (result.isFinal()) {
					// We've finished this task. Queue an event and store the return values
					returnValues = result.getResult();
					access.queueEvent(EVENT_NAME, new Object[]{id, true});
					return true;
				} else {
					// We've got something else to execute: reset the task
					task = result.getCallback();
					remaining = result.getDelay();
					return false;
				}
			} catch (LuaException e) {
				yieldFailure(e.getMessage());
				return true;
			} catch (Throwable e) {
				DebugLogger.error("Error in task: ", e);
				yieldFailure("Java Exception Thrown: " + e.toString());
				return true;
			}
		}
	}
}
