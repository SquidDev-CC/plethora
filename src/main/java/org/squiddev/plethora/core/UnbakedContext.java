package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;

/**
 * A context which doesn't have solidified references.
 */
public final class UnbakedContext<T> implements IUnbakedContext<T> {
	private final IReference<T> target;
	private final IReference<?>[] context;
	private final ICostHandler handler;
	private final IReference<IModuleContainer> modules;
	private final IResultExecutor executor;

	public UnbakedContext(IReference<T> target, IReference<?>[] context, ICostHandler handler, IReference<IModuleContainer> modules, IResultExecutor executor) {
		this.target = target;
		this.handler = handler;
		this.context = context;
		this.modules = modules;
		this.executor = executor;
	}

	@Nonnull
	@Override
	public IContext<T> bake() throws LuaException {
		T value = target.get();

		Object[] baked = new Object[context.length];
		for (int i = baked.length - 1; i >= 0; i--) {
			baked[i] = context[i].get();
		}

		return new Context<T>(this, value, baked, handler, modules.get());
	}

	@Nonnull
	@Override
	public IContext<T> safeBake() throws LuaException {
		T value = target.safeGet();

		Object[] baked = new Object[context.length];
		for (int i = baked.length - 1; i >= 0; i--) {
			baked[i] = context[i].safeGet();
		}

		return new Context<T>(this, value, baked, handler, modules.safeGet());
	}

	@Nonnull
	@Override
	public <U> IUnbakedContext<U> makeChild(@Nonnull IReference<U> newTarget, @Nonnull IReference<?>... newContext) {
		Preconditions.checkNotNull(newTarget, "target cannot be null");
		Preconditions.checkNotNull(newContext, "context cannot be null");

		IReference<?>[] wholeContext = new IReference<?>[newContext.length + context.length + 1];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);
		wholeContext[wholeContext.length - 1] = target;

		return new UnbakedContext<U>(newTarget, wholeContext, handler, modules, executor);
	}

	@Nonnull
	@Override
	public IUnbakedContext<T> withContext(@Nonnull IReference<?>... newContext) {
		Preconditions.checkNotNull(newContext, "context cannot be null");

		IReference<?>[] wholeContext = new IReference<?>[newContext.length + context.length];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);

		return new UnbakedContext<T>(target, wholeContext, handler, modules, executor);
	}

	@Override
	public IUnbakedContext<T> withCostHandler(@Nonnull ICostHandler handler) {
		Preconditions.checkNotNull(handler, "handler cannot be null");
		return new UnbakedContext<T>(target, context, handler, modules, executor);
	}

	@Override
	public IUnbakedContext<T> withModules(@Nonnull IReference<IModuleContainer> modules) {
		Preconditions.checkNotNull(modules, "modules cannot be null");
		return new UnbakedContext<T>(target, context, handler, modules, executor);
	}

	@Override
	public IUnbakedContext<T> withExecutor(@Nonnull IResultExecutor executor) {
		Preconditions.checkNotNull(executor, "executor cannot be null");
		return new UnbakedContext<T>(target, context, handler, modules, executor);
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler() {
		return handler;
	}

	@Nonnull
	@Override
	public IResultExecutor getExecutor() {
		return executor;
	}

	public static <T> void arrayCopy(T[] src, T[] to, int start) {
		System.arraycopy(src, 0, to, start, src.length);
	}
}
