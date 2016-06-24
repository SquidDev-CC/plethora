package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;

@Method(Object.class)
public class MethodMeta extends BasicMethod<Object> {
	public MethodMeta() {
		super("getMetadata", true);
	}

	@Override
	public boolean canApply(IContext<Object> context) {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		return registry.getMetaProviders(context.getTarget().getClass()).size() > 0;
	}

	@Override
	public Object[] apply(IContext<Object> context, Object[] args) throws LuaException {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		return new Object[]{registry.getMeta(context.getTarget())};
	}
}
