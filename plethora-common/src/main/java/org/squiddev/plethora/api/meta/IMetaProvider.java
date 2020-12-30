package org.squiddev.plethora.api.meta;

import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Provides metadata about an object.
 *
 * @see IMetaRegistry
 * @see IPartialContext#getMeta()
 * @see org.squiddev.plethora.api.Injects
 */
@FunctionalInterface
public interface IMetaProvider<T> {
	/**
	 * Get metadata about an object
	 *
	 * @param context The object to get metadata about
	 * @return The gathered data. Do not return {@code null}.
	 */
	@Nonnull
	Map<String, ?> getMeta(@Nonnull IPartialContext<T> context);

	/**
	 * Get the priority of this provider
	 *
	 * {@link Integer#MIN_VALUE} is the lowest priority and {@link Integer#MAX_VALUE} is the highest. Providers
	 * with higher priorities will be preferred.
	 *
	 * @return The provider's priority
	 */
	default int getPriority() {
		return 0;
	}

	/**
	 * Get a basic description of this meta provider
	 *
	 * @return This provider's description, or {@code null} if none is available.
	 */
	@Nullable
	default String getDescription() {
		return null;
	}

	/**
	 * Get an example input for this meta provider
	 *
	 * @return An example input for this meta provider, or {@code null} if none is available.
	 */
	@Nullable
	default T getExample() {
		return null;
	}
}
