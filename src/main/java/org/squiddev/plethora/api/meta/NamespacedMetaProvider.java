package org.squiddev.plethora.api.meta;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Map;

/**
 * A provider that wraps the data in a namespace
 *
 * @see IMetaRegistry#registerMetaProvider(Class, String, IMetaProvider)
 */
public class NamespacedMetaProvider<T> implements IMetaProvider<T> {
	private final String namespace;
	private final IMetaProvider<T> delegate;

	/**
	 * Create a new namespaced metadata provider
	 *
	 * @param namespace The namespace to insert into
	 * @param delegate  The provider to delegate to
	 */
	public NamespacedMetaProvider(String namespace, IMetaProvider<T> delegate) {
		Preconditions.checkNotNull(namespace, "namespace cannot be null");
		Preconditions.checkNotNull(delegate, "delegate cannot be null");

		this.namespace = namespace;
		this.delegate = delegate;
	}

	@Override
	public Map<Object, Object> getMeta(T object) {
		Map<Object, Object> data = delegate.getMeta(object);
		if (data.size() > 0) {
			return Collections.<Object, Object>singletonMap(namespace, data);
		} else {
			return Collections.emptyMap();
		}
	}
}
