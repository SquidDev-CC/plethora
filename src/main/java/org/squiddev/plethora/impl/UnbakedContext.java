package org.squiddev.plethora.impl;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.reference.IReference;

/**
 * A context which doesn't have solidified references.
 */
public class UnbakedContext<T> implements IUnbakedContext<T> {
	private final IReference<T> target;
	private final IReference<?>[] context;

	public UnbakedContext(IReference<T> target, IReference<?>... context) {
		this.target = target;
		this.context = context;
	}

	@Override
	public IContext<T> bake() throws LuaException {
		T value = target.get();

		Object[] baked = new Object[context.length];
		for (int i = baked.length - 1; i >= 0; i--) {
			baked[i] = context[i].get();
		}

		return new Context<T>(this, value);
	}

	@Override
	public <U> IUnbakedContext<U> makeChild(IReference<U> newTarget, IReference<?>... newContext) {
		Preconditions.checkNotNull(newTarget, "target cannot be null");
		Preconditions.checkNotNull(newContext, "context cannot be null");

		IReference<?>[] wholeContext = new IReference<?>[newContext.length + context.length + 1];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);
		wholeContext[wholeContext.length - 1] = target;

		return new UnbakedContext<U>(newTarget, wholeContext);
	}

	@Override
	public IUnbakedContext<T> withContext(IReference<?>... newContext) {
		Preconditions.checkNotNull(newContext, "context cannot be null");

		IReference<?>[] wholeContext = new IReference<?>[newContext.length + context.length];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);

		return new UnbakedContext<T>(target, wholeContext);
	}

	private static void arrayCopy(Object[] src, Object[] to, int start) {
		System.arraycopy(src, 0, to, start, src.length);
	}
}
