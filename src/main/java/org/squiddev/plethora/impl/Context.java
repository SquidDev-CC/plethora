package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import org.squiddev.plethora.api.method.IContext;

public class Context<T> implements IContext<T> {
	private final T target;
	private final Object[] context;


	public Context(T target, Object[] context) {
		this.target = target;
		this.context = context;
	}

	@Override
	public T getTarget() {
		return target;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return true;
		}

		return false;
	}
}
