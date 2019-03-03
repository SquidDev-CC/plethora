package org.squiddev.plethora.integration;

import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IConverterExcludeMethod;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;

@Injects
public final class MethodMeta extends BasicObjectMethod<Object> implements IConverterExcludeMethod {
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
	public Object[] apply(@Nonnull IContext<Object> context, @Nonnull Object[] args) {
		return new Object[]{context.getMeta()};
	}
}
