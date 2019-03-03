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
 * @see FutureTask
 */
public abstract class Task {
	private volatile boolean done = false;
	private Callable<MethodResult> callback;
	private MethodResult.Resolver resolver;

	public Task(Callable<MethodResult> callback, MethodResult.Resolver resolver) {
		this.callback = callback;
		this.resolver = resolver;
	}

	protected abstract void finish(@Nullable Object[] result);

	protected abstract void finish(@Nonnull LuaException e);

	protected void submitTiming(long time) {
	}

	public boolean update() {
		while (resolver.update()) {
			long start = System.nanoTime();
			try {
				MethodResult next = callback.call();
				if (next.isFinal()) {
					markFinished();
					finish(next.getResult());
					return true;
				} else {
					resolver = next.getResolver();
					callback = next.getCallback();
				}
			} catch (LuaException e) {
				markFinished();
				finish(e);
				return true;
			} catch (Throwable e) {
				markFinished();
				finish(new LuaException("Java Exception Thrown: " + e.toString()));
				PlethoraCore.LOG.error("Unexpected error", e);
				return true;
			} finally {
				submitTiming(System.nanoTime() - start);
			}
		}

		return false;
	}

	protected final void markFinished() {
		done = true;
	}

	public boolean finished() {
		return done;
	}
}
