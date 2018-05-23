package org.squiddev.plethora.core.executor;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.utils.DebugLogger;

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

	public boolean update() {
		if (!resolver.update()) return false;

		try {
			MethodResult next = callback.call();
			if (next.isFinal()) {
				finish(next.getResult());
				markFinished();
				return true;
			} else {
				resolver = next.getResolver();
				callback = next.getCallback();
				return false;
			}
		} catch (LuaException e) {
			finish(e);
			markFinished();
			return true;
		} catch (Throwable e) {
			finish(new LuaException("Java Exception Thrown: " + e.toString()));
			markFinished();
			DebugLogger.error("Unexpected error", e);
			return true;
		}
	}

	protected final void markFinished() {
		done = true;
	}

	public boolean finished() {
		return done;
	}
}
