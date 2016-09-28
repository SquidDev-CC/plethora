package org.squiddev.plethora.api.meta;

/**
 * Basic wrapper for meta-providers
 */
public abstract class BaseMetaProvider<T> implements IMetaProvider<T> {
	private final int priority;

	public BaseMetaProvider(int priority) {
		this.priority = priority;
	}

	public BaseMetaProvider() {
		this.priority = 0;
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
