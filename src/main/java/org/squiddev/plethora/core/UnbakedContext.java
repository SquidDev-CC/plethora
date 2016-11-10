package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * A context which doesn't have solidified references.
 */
public final class UnbakedContext<T> implements IUnbakedContext<T> {
	private final IReference<T> target;
	private final IReference<?>[] context;
	private final ICostHandler handler;
	protected final IReference<Set<ResourceLocation>> modules;

	public UnbakedContext(IReference<T> target, ICostHandler handler, IReference<?>[] context, IReference<Set<ResourceLocation>> modules) {
		this.target = target;
		this.handler = handler;
		this.context = context;
		this.modules = modules;
	}

	@Nonnull
	@Override
	public IContext<T> bake() throws LuaException {
		T value = target.get();

		Object[] baked = new Object[context.length];
		for (int i = baked.length - 1; i >= 0; i--) {
			baked[i] = context[i].get();
		}

		return new Context<T>(this, value, handler, baked, modules.get());
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

		return new UnbakedContext<U>(newTarget, handler, wholeContext, modules);
	}

	@Nonnull
	@Override
	public IUnbakedContext<T> withContext(@Nonnull IReference<?>... newContext) {
		Preconditions.checkNotNull(newContext, "context cannot be null");

		IReference<?>[] wholeContext = new IReference<?>[newContext.length + context.length];
		arrayCopy(newContext, wholeContext, 0);
		arrayCopy(context, wholeContext, newContext.length);

		return new UnbakedContext<T>(target, handler, wholeContext, modules);
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		IContext<T> baked = tryBake(this);
		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> pair = MethodRegistry.instance.getMethodsPaired(this, baked);

		return new MethodWrapperLuaObject(pair.getFirst(), pair.getSecond(), baked.getContext(IComputerAccess.class));
	}

	@Nonnull
	@Override
	public ICostHandler getCostHandler() {
		return handler;
	}

	public static void arrayCopy(Object[] src, Object[] to, int start) {
		System.arraycopy(src, 0, to, start, src.length);
	}

	public static <T> IContext<T> tryBake(IUnbakedContext<T> context) {
		try {
			return context.bake();
		} catch (LuaException e) {
			throw new IllegalStateException("Error occurred when baking", e);
		}
	}
}
