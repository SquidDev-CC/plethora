package org.squiddev.plethora.api.meta;

import java.util.Collection;
import java.util.Map;

/**
 * A registry for metadata providers.
 *
 * @see IMetaProvider
 */
public interface IMetaRegistry {
	/**
	 * Register a metadata provider
	 *
	 * @param target   The class this provider targets
	 * @param provider The relevant provider
	 */
	<T> void registerMetaProvider(Class<T> target, IMetaProvider<T> provider);

	/**
	 * Register a metadata provider
	 *
	 * @param target    The class this provider targets
	 * @param namespace The namespace to put this data under.
	 * @param provider  The relevant provider
	 */
	<T> void registerMetaProvider(Class<T> target, String namespace, IMetaProvider<T> provider);

	/**
	 * Get metadata about an object
	 *
	 * @param object The object to get metadata about
	 * @return The gathered metadata
	 */
	Map<String, Object> getMeta(Object object);

	/**
	 * An list of all valid providers for a class
	 *
	 * @param target The class to get data about
	 * @return List of valid providers
	 */
	Collection<IMetaProvider<?>> getMetaProviders(Class<?> target);
}
