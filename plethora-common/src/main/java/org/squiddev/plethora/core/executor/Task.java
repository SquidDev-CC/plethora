package org.squiddev.plethora.core.executor;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * Represents an evaluation of a {@link MethodResult} on the main thread.
 *
 * This will wait for the requirement to be resolved, execute the callback and either provide an exception, the result
 * or continue to unwrap the next {@link MethodResult}.
 *
 * @see TaskRunner
 */
public class Task {
	private volatile boolean done = false;
	private Callable<MethodResult> callback;
	private MethodResult.Resolver resolver;
	private boolean resolved;

	Object[] result;
	LuaException error;

	Task(Callable<MethodResult> callback, MethodResult.Resolver resolver) {
		this.callback = callback;
		this.resolver = resolver;
	}

	private void finish(@Nullable Object[] result) {
		done = true;
		this.result = result;
		whenDone();
	}

	private void finish(@Nonnull LuaException error) {
		done = true;
		this.error = error;
		whenDone();
	}

	public boolean update() {
		while (!done && (resolved || (resolved = resolver.update()) && canWork())) {
			long start = System.nanoTime();
			try {
				MethodResult next = callback.call();
				if (next.isFinal()) {
					finish(next.getResult());
					return true;
				}
				resolver = next.getResolver();
				resolved = false;
				callback = next.getCallback();
			} catch (LuaException e) {
				finish(e);
				return true;
			} catch (Exception | LinkageError | VirtualMachineError e) {
				finish(new LuaException("Java Exception Thrown: " + e));
				PlethoraCore.LOG.error("Unexpected error", e);
				return true;
			} catch (Error e) {
				finish(new LuaException("Java Exception Thrown: " + e));
				PlethoraCore.LOG.error("Unexpected error", e);
				throw e;
			} finally {
				submitTiming(System.nanoTime() - start);
			}
		}

		return done;
	}

	final void cancel() {
		done = true;
	}

	final boolean isDone() {
		return done;
	}

	/**
	 * Finalise the task (fire events, etc...)
	 */
	void whenDone() {
	}

	/**
	 * Submit some timings for a task
	 *
	 * @param time How long this task took
	 */
	void submitTiming(long time) {
	}

	/**
	 * Determine we should continue to work after executing a task
	 *
	 * @return If we should continue to work
	 */
	boolean canWork() {
		return true;
	}
}
