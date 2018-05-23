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
	private final Resolver resolver;

	private MethodResult(Object[] result) {
		this.result = result;
		this.next = null;
		this.resolver = IMMEDIATE;
	}

	private MethodResult(Callable<MethodResult> next, Resolver resolver) {
		Preconditions.checkNotNull(next, "next cannot be null");
		Preconditions.checkNotNull(resolver, "resolver cannot be null");

		this.result = null;
		this.next = next;
		this.resolver = resolver;
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

	public Resolver getResolver() {
		if (isFinal()) throw new IllegalStateException("MethodResult is a result");
		return resolver;
	}

	/**
	 * Defer a function until next tick
	 *
	 * @param next The callback to execute
	 * @return The built MethodResult
	 * @see #nextTick(Runnable)
	 */
	public static MethodResult nextTick(Callable<MethodResult> next) {
		return new MethodResult(next, IMMEDIATE);
	}

	/**
	 * Defer a function until next tick
	 *
	 * @param next The callback to execute
	 * @return The built MethodResult
	 * @see #nextTick(Callable)
	 */
	public static MethodResult nextTick(Runnable next) {
		return new MethodResult(wrap(next), IMMEDIATE);
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
		return new MethodResult(next, delay <= 0 ? IMMEDIATE : new DelayedResolver(delay));
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
		return new MethodResult(wrap(next), delay <= 0 ? IMMEDIATE : new DelayedResolver(delay));
	}

	/**
	 * Execute a method when the resolver evaluates to true
	 *
	 * @param resolver The resolver to wait on
	 * @param next     The callback to execute
	 * @return THe built MethodResult
	 */
	public static MethodResult awaiting(Resolver resolver, Callable<MethodResult> next) {
		return new MethodResult(next, resolver);
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
	 * Get a final method MethodResult which will finish after a number of ticks
	 *
	 * @param delay The number of ticks to sit idle before executing. 0 will result in the method being executed n
	 * @param args  The arguments to return
	 * @return The built MethodResult
	 */
	public static MethodResult delayedResult(int delay, final Object... args) {
		return delay <= 0
			? new MethodResult(args)
			: new MethodResult(() -> MethodResult.result(args), new DelayedResolver(delay));
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
		return () -> {
			runnable.run();
			return empty;
		};
	}

	public interface Resolver {
		boolean update();
	}

	private static class DelayedResolver implements Resolver {
		private int remaining;

		DelayedResolver(int remaining) {
			this.remaining = remaining;
		}

		@Override
		public boolean update() {
			return remaining-- == 0;
		}
	}

	private static final Resolver IMMEDIATE = () -> true;
}
