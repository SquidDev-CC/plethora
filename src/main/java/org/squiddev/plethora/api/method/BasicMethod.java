package org.squiddev.plethora.api.method;

import javax.annotation.Nonnull;

/**
 * Basic wrapper for methods
 */
public abstract class BasicMethod<T> implements IMethod<T> {
	private final String name;
	private final boolean worldThread;
	private final int priority;

	public BasicMethod(String name, boolean worldThread, int priority) {
		this.name = name;
		this.worldThread = worldThread;
		this.priority = priority;
	}

	public BasicMethod(String name, boolean worldThread) {
		this.name = name;
		this.worldThread = worldThread;
		this.priority = 0;
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final boolean worldThread() {
		return worldThread;
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
