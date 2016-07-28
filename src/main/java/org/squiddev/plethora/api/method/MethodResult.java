package org.squiddev.plethora.api.method;

import com.google.common.base.Preconditions;

import java.util.concurrent.Callable;

/**
 * The result of a method
 */
public final class MethodResult {
	private static final MethodResult empty = new MethodResult(null);

	private final Object[] result;
	private final Callable<MethodResult> next;
	private final int delay;

	private MethodResult(Object[] result) {
		this.result = result;
		this.next = null;
		this.delay = -1;
	}

	private MethodResult(Callable<MethodResult> next, int delay) {
		Preconditions.checkNotNull(next, "next cannot be null");
		Preconditions.checkArgument(delay >= 0, "delay must be >= 0");

		this.result = null;
		this.next = next;
		this.delay = delay;
	}

	public boolean isFinal() {
		return next == null;
	}

	public Object[] getResult() {
		if (!isFinal()) throw new IllegalStateException("MethodResult is a callback");
		return result;
	}

	public Callable<MethodResult> getCallback() {
		if (isFinal()) throw new IllegalStateException("MethodResult is a result");
		return next;
	}

	public int getDelay() {
		if (isFinal()) throw new IllegalStateException("MethodResult is a result");
		return delay;
	}

	/**
	 * Defer a function until next tick
	 *
	 * @param next The callback to execute
	 * @return The built MethodResult
	 * @see #nextTick(Runnable)
	 */
	public static MethodResult nextTick(Callable<MethodResult> next) {
		return new MethodResult(next, 0);
	}

	/**
	 * Defer a function until next tick
	 *
	 * @param next The callback to execute
	 * @return The built MethodResult
	 * @see #nextTick(Callable)
	 */
	public static MethodResult nextTick(Runnable next) {
		return new MethodResult(wrap(next), 0);
	}

	/**
	 * Delay a function by a number of ticks
	 *
	 * @param delay The number of ticks to sit idle before executing. 0 will result in the method being executed next tick.
	 * @param next  The callback to execute
	 * @return The built MethodResult
	 * @see #delayed(int, Runnable)
	 */
	public static MethodResult delayed(int delay, Callable<MethodResult> next) {
		return new MethodResult(next, delay);
	}

	/**
	 * Delay a function by a number of ticks
	 *
	 * @param delay The number of ticks to sit idle before executing. 0 will result in the method being executed n
	 * @param next  The callback to execute
	 * @return The built MethodResult
	 * @see #delayed(int, Callable)
	 */
	public static MethodResult delayed(int delay, Runnable next) {
		return new MethodResult(wrap(next), delay);
	}

	/**
	 * Get a final method MethodResult
	 *
	 * @param args The arguments to return
	 * @return The built MethodResult
	 */
	public static MethodResult result(Object... args) {
		return new MethodResult(args);
	}

	/**
	 * Get a final MethodResult representing a failure
	 *
	 * @param message The failure message
	 * @return The built MethodResult
	 */
	public static MethodResult failure(String message) {
		return new MethodResult(new Object[]{false, message});
	}

	/**
	 * Get a final MethodResult with no values
	 *
	 * @return An empty MethodResult
	 */
	public static MethodResult empty() {
		return empty;
	}

	private static Callable<MethodResult> wrap(final Runnable runnable) {
		return new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				runnable.run();
				return empty;
			}
		};
	}
}
