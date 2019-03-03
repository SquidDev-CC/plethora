package org.squiddev.plethora.api.meta;

import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * A registry for metadata providers.
 *
 * @see IMetaProvider
 */
public interface IMetaRegistry {
	/**
	 * Get metadata about an object
	 *
	 * @param context The object to get metadata about
	 * @return The gathered metadata
	 */
	@Nonnull
	Map<Object, Object> getMeta(@Nonnull IPartialContext<?> context);

	/**
	 * An list of all valid providers for a class
	 *
	 * @param target The class to get data about
	 * @return List of valid providers
	 */
	@Nonnull
	List<IMetaProvider<?>> getMetaProviders(@Nonnull Class<?> target);
}
