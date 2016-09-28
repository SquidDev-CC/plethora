package org.squiddev.plethora.api.meta;

import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Basic wrapper for meta-providers
 */
public abstract class BasicMetaProvider<T> extends BaseMetaProvider<T> {
	public BasicMetaProvider(int priority) {
		super(priority);
	}

	public BasicMetaProvider() {
	}

	public abstract Map<Object, Object> getMeta(@Nonnull T object);

	@Nonnull
	@Override
	public final Map<Object, Object> getMeta(@Nonnull IPartialContext<T> object) {
		return getMeta(object.getTarget());
	}
}
