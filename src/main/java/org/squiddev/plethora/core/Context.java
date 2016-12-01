package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaObject;
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

public class Context<T> extends PartialContext<T> implements IContext<T> {
	private final IUnbakedContext<T> parent;

	public Context(@Nonnull IUnbakedContext<T> parent, @Nonnull T target, @Nonnull ICostHandler handler, @Nonnull Object[] context, @Nonnull Set<ResourceLocation> modules) {
		super(target, handler, context, modules);
		this.parent = parent;
	}

	@Nonnull
	@Override
	public <U> IUnbakedContext<U> makeChild(@Nonnull IReference<U> target, @Nonnull IReference<?>... context) {
		return parent.makeChild(target, context);
	}

	@Nonnull
	@Override
	public IUnbakedContext<T> unbake() {
		return parent;
	}

	@Nonnull
	@Override
	public ILuaObject getObject() {
		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> pair = MethodRegistry.instance.getMethodsPaired(parent, this);
		return new MethodWrapperLuaObject(pair.getFirst(), pair.getSecond());
	}
}
