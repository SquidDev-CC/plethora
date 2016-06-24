package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.reference.IReference;

public class Context<T> implements IContext<T> {
	private final IUnbakedContext<T> parent;
	private final T target;
	private final Object[] context;

	Context(IUnbakedContext<T> parent, T target, Object... context) {
		this.parent = parent;
		this.target = target;
		this.context = context;
	}

	@Override
	public T getTarget() {
		return target;
	}

	public Object[] getContext() {
		return context;
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

	@Override
	public <U> IUnbakedContext<U> makeChild(IReference<U> target, IReference<?>... context) {
		return parent.makeChild(target, context);
	}

	@Override
	public IUnbakedContext<T> withContext(IReference<?>... context) {
		return parent.withContext(context);
	}
}
