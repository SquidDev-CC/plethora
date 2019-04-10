package org.squiddev.plethora.api.meta;

import javax.annotation.Nonnull;

/**
 * Basic wrapper for meta-providers
 */
public abstract class BaseMetaProvider<T> implements IMetaProvider<T> {
	private final int priority;
	private final String description;

	public BaseMetaProvider(int priority, String description) {
		this.priority = priority;
		this.description = description;
	}

	public BaseMetaProvider(String description) {
		this(0, description);
	}

	public BaseMetaProvider(int priority) {
		this(priority, null);
	}

	public BaseMetaProvider() {
		this(0, null);
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Nonnull
	@Override
	public String getDescription() {
		return description;
	}
}
