package org.squiddev.plethora.integration;

import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;

@Injects
public final class MethodMeta extends BasicMethod<Object> implements IConverterExcludeMethod {
	public MethodMeta() {
		super("getMetadata", Integer.MIN_VALUE, "function():table -- Get metadata about this object");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<Object> context) {
		IMetaRegistry registry = PlethoraAPI.instance().metaRegistry();
		Object target = context.getTarget();

		if (!registry.getMetaProviders(target.getClass()).isEmpty()) return true;

		// Convert all and check if any matches
		for (Object converted : PlethoraAPI.instance().converterRegistry().convertAll(target)) {
			if (!registry.getMetaProviders(converted.getClass()).isEmpty()) return true;
		}

		return false;
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<Object> context, @Nonnull Object[] args) {
		return MethodResult.nextTick(() -> MethodResult.result(context.bake().getMeta()));
	}
}
