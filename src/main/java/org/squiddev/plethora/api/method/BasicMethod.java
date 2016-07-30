package org.squiddev.plethora.api.method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A basic wrapper for methods
 */
public abstract class BasicMethod<T> implements IMethod<T> {
	private final String name;
	private final String docs;
	private final int priority;

	public BasicMethod(String name) {
		this(name, 0, null);
	}

	public BasicMethod(String name, int priority) {
		this(name, priority, null);
	}

	public BasicMethod(String name, String docs) {
		this(name, 0, docs);
	}

	public BasicMethod(String name, int priority, String docs) {
		this.name = name;
		this.priority = priority;
		this.docs = docs;
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

	@Nullable
	@Override
	public String getDocString() {
		return docs;
	}
}
