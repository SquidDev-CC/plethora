package org.squiddev.plethora.utils;

import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;

import javax.annotation.Nonnull;

public class ContextHelpers {
	public static <T> T getOriginOr(@Nonnull IContext<?> context, @Nonnull String otherKey, @Nonnull Class<T> klass) {
		T object = context.getContext(ContextKeys.ORIGIN, klass);
		return object == null ? context.getContext(otherKey, klass) : object;
	}
}
