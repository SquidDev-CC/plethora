package org.squiddev.plethora.api.meta;

import javax.annotation.Nonnull;
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
	@Nonnull
	Map<Object, Object> getMeta(@Nonnull T object);

	/**
	 * Get the priority of this provider
	 *
	 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Providers
	 * with higher priorities will be preferred.
	 *
	 * @return The provider's priority
	 */
	int getPriority();
}
