package org.squiddev.plethora.core.wrapper;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.ISubTargetedMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleMethod;

import javax.annotation.Nonnull;
import java.util.Collection;

class RenamedMethod<T, U> implements IModuleMethod<T>, ISubTargetedMethod<T, U> {
	private final String name;
	private final MethodInstance<T, U> instance;

	RenamedMethod(String name, MethodInstance<T, U> instance) {
		this.name = name;
		this.instance = instance;
	}

	@Override
	public Class<U> getSubTarget() {
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

	@Nonnull
	@Override
	public String getId() {
		return instance.getId();
	}

	@Override
	public boolean has(@Nonnull Class<?> iface) {
		return instance.has(iface);
	}
}
