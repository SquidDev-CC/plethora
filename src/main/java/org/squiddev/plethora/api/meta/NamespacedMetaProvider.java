package org.squiddev.plethora.api.meta;

import com.google.common.base.Preconditions;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * A provider that wraps the data in a namespace
 *
 * @see IMetaRegistry#registerMetaProvider(Class, String, IMetaProvider)
 */
public class NamespacedMetaProvider<T> extends BaseMetaProvider<T> {
	private final String namespace;
	private final IMetaProvider<T> delegate;

	/**
	 * Create a new namespaced metadata provider
	 *
	 * @param namespace The namespace to insert into
	 * @param delegate  The provider to delegate to
	 */
	public NamespacedMetaProvider(String namespace, IMetaProvider<T> delegate) {
		super(delegate.getPriority(), delegate.getDescription());
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(delegate, "delegate cannot be null");

		this.namespace = namespace;
		this.delegate = delegate;
	}

	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<T> context) {
		Map<Object, Object> data = delegate.getMeta(context);
		if (data.size() > 0) {
			return Collections.singletonMap(namespace, data);
		} else {
			return Collections.emptyMap();
		}
	}

	@Nullable
	@Override
	public T getExample() {
		return delegate.getExample();
	}

	public IMetaProvider<T> getDelegate() {
		return delegate;
	}
}
