package org.squiddev.plethora.api.meta;

/**
 * Basic wrapper for meta-providers
 */
public abstract class BasicMetaProvider<T> implements IMetaProvider<T> {
	private final int priority;

	public BasicMetaProvider(int priority) {
		this.priority = priority;
	}

	public BasicMetaProvider() {
		this.priority = 0;
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
