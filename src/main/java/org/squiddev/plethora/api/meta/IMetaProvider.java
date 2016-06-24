package org.squiddev.plethora.api.meta;

import java.util.Map;

/**
 * Provides metadata about an object
 * Register with {@link IMetaRegistry}
 */
public interface IMetaProvider<T> {
	/**
	 * Get metadata about an object
	 *
	 * @param object The object to get metadata about
	 * @return The gathered data. Do not return {@code null}.
	 */
	Map<String, Object> getMeta(T object);
}
