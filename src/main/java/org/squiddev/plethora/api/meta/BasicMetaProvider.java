package org.squiddev.plethora.api.meta;

import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Basic wrapper for meta-providers
 */
public abstract class BasicMetaProvider<T> extends BaseMetaProvider<T> implements SimpleMetaProvider<T> {
	public BasicMetaProvider(int priority, String description) {
		super(priority, description);
	}

	public BasicMetaProvider(String description) {
		super(description);
	}

	public BasicMetaProvider(int priority) {
		super(priority);
	}

	public BasicMetaProvider() {
	}

	@Nonnull
	@Override
	public final Map<Object, Object> getMeta(@Nonnull IPartialContext<T> context) {
		return getMeta(context.getTarget());
	}
}
