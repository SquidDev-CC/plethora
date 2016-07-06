package org.squiddev.plethora.api.method;

import javax.annotation.Nonnull;

/**
 * A basic wrapper for methods
 */
public abstract class BasicMethod<T> implements IMethod<T> {
	private final String name;
	private final int priority;

	public BasicMethod(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	public BasicMethod(String name) {
		this.name = name;
		this.priority = 0;
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public boolean canApply(@Nonnull IContext<T> context) {
		return true;
	}

	@Override
	public final int getPriority() {
		return priority;
	}
}
