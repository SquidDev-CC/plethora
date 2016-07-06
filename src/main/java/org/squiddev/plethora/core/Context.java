package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.Tuple;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.core.UnbakedContext.arrayCopy;

public class Context<T> implements IContext<T> {
	private final IUnbakedContext<T> parent;
	private final T target;
	private final Object[] context;

	public Context(IUnbakedContext<T> parent, T target, Object... context) {
		this.parent = parent;
		this.target = target;
		this.context = context;
	}

	@Nonnull
	@Override
	public T getTarget() {
		return target;
	}

	public Object[] getContext() {
		return context;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V getContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return (V) obj;
		}

		return null;
	}

	@Override
	public <V> boolean hasContext(@Nonnull Class<V> klass) {
		Preconditions.checkNotNull(klass, "klass cannot be null");

		for (int i = context.length - 1; i >= 0; i--) {
			Object obj = context[i];
			if (klass.isInstance(obj)) return true;
		}

		return false;
	}

	@Nonnull
	@Override
	public <U> IUnbakedContext<U> makeChild(@Nonnull IReference<U> target, @Nonnull IReference<?>... context) {
		Preconditions.checkNotNull(parent, "This is not a fully fleshed context");
		return parent.makeChild(target, context);
	}

	@Nonnull
	@Override
	public <U> IContext<U> makeBakedChild(@Nonnull U newTarget, @Nonnull Object... newContext) {
		Preconditions.checkNotNull(newTarget, "target cannot be null");
		Preconditions.checkNotNull(newContext, "context cannot be null");

		Object[] wholeContext = new Object[newContext.length + context.length + 1];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);
		wholeContext[wholeContext.length - 1] = target;

		return new Context<U>(null, newTarget, wholeContext);
	}

	@Nonnull
	@Override
	public IUnbakedContext<T> withContext(@Nonnull IReference<?>... context) {
		Preconditions.checkNotNull(parent, "This is not a fully fleshed context");
		return parent.withContext(context);
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		Preconditions.checkNotNull(parent, "This is not a fully fleshed context");

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> pair = MethodRegistry.instance.getMethodsPaired(parent, this);
		return new MethodWrapperLuaObject(pair.getFirst(), pair.getSecond(), getContext(IComputerAccess.class));
	}
}
