package org.squiddev.plethora.core.wrapper;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import java.util.Collection;

class RenamedMethod<T> implements IMethod<T> {
	private final String name;
	private final MethodInstance<T> instance;

	RenamedMethod(String name, MethodInstance<T> instance) {
		this.name = name;
		this.instance = instance;
	}

	@Override
	public Class<?> getSubTarget() {
		return instance.getSubTarget();
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> getModules() {
		return instance.getModules();
	}

	@Nonnull
	@Override
	public String getName() {
		return name;
	}

	@Nonnull
	@Override
	public String getDocString() {
		return instance.getDocString();
	}

	@Override
	public int getPriority() {
		return instance.getPriority();
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<T> context) {
		return instance.canApply(context);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<T> context, @Nonnull Object[] args) throws LuaException {
		return instance.apply(context, args);
	}

	@Override
	public boolean has(@Nonnull Class<?> iface) {
		return instance.has(iface);
	}
}
