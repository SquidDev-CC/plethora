package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;

import javax.annotation.Nonnull;

@IMethod.Inject(Object.class)
public class MethodMeta extends BasicObjectMethod<Object> {
	public MethodMeta() {
		super("getMetadata", true, Integer.MIN_VALUE, "function():table -- Get metadata about this object");
	}

	@Override
	public boolean canApply(@Nonnull IContext<Object> context) {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		return registry.getMetaProviders(context.getTarget().getClass()).size() > 0;
	}

	@Override
	public Object[] apply(@Nonnull IContext<Object> context, @Nonnull Object[] args) throws LuaException {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		return new Object[]{registry.getMeta(context.getTarget())};
	}
}
