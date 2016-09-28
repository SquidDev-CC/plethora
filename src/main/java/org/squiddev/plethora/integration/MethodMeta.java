package org.squiddev.plethora.integration;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;

@IMethod.Inject(Object.class)
public class MethodMeta extends BasicObjectMethod<Object> implements IConverterExcludeMethod {
	public MethodMeta() {
		super("getMetadata", true, Integer.MIN_VALUE, "function():table -- Get metadata about this object");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<Object> context) {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		Object target = context.getTarget();

		if (registry.getMetaProviders(target.getClass()).size() > 0) return true;

		// Convert all and check if any matches
		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(target)) {
			if (registry.getMetaProviders(converted.getClass()).size() > 0) return true;
		}

		return false;
	}

	@Override
	public Object[] apply(@Nonnull IContext<Object> context, @Nonnull Object[] args) throws LuaException {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		return new Object[]{registry.getMeta(context.getTarget())};
	}
}
