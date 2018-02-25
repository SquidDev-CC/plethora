package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.ILuaObject;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.core.UnbakedContext.arrayCopy;

public class Context<T> extends PartialContext<T> implements IContext<T> {
	private final IUnbakedContext<T> parent;

	public Context(@Nonnull IUnbakedContext<T> parent, @Nonnull T target, @Nonnull Object[] context, @Nonnull ICostHandler handler, @Nonnull IModuleContainer modules) {
		super(target, context, handler, modules);
		this.parent = parent;
	}

	@Nonnull
	@Override
	public <U> IContext<U> makeChild(U target, @Nonnull IReference<U> targetReference) {
		Preconditions.checkNotNull(target, "target cannot be null");
		Preconditions.checkNotNull(targetReference, "targetReference cannot be null");

		IUnbakedContext<U> child = parent.makeChild(targetReference);

		Object[] context = getContext();
		Object[] wholeContext = new Object[context.length + 1];
		arrayCopy(context, wholeContext, 0);
		wholeContext[wholeContext.length - 1] = target;

		return new Context<U>(child, target, wholeContext, getCostHandler(), getModules());
	}

	@Nonnull
	@Override
	public <U extends IReference<U>> IContext<U> makeChild(@Nonnull U target) {
		return makeChild(target, target);
	}

	@Nonnull
	@Override
	public <U> IContext<U> makeChildId(@Nonnull U target) {
		return makeChild(target, Reference.id(target));
	}

	@Nonnull
	@Override
	public IUnbakedContext<T> unbake() {
		return parent;
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> pair = MethodRegistry.instance.getMethodsPaired(parent, this);
		return new MethodWrapperLuaObject(pair.getLeft(), pair.getRight());
	}
}
