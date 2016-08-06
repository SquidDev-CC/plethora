package org.squiddev.plethora.api.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * The primary registry for transfer providers
 */
public interface ITransferRegistry {

	/**
	 * Register a primary transfer provider
	 *
	 * @param klass    The class this provider targets
	 * @param provider The provider instance
	 */
	<T> void registerPrimary(@Nonnull Class<T> klass, @Nonnull ITransferProvider<T> provider);

	/**
	 * Register a secondary transfer provider
	 *
	 * @param klass    The class this provider targets
	 * @param provider The provider instance
	 */
	<T> void registerSecondary(@Nonnull Class<T> klass, @Nonnull ITransferProvider<T> provider);

	/**
	 * Get all primary providers for a class
	 *
	 * @param klass The class to get them f or
	 * @return All primary providers
	 */
	@Nonnull
	<T> Collection<ITransferProvider<? super T>> getPrimaryProviders(@Nonnull Class<T> klass);

	/**
	 * Get all secondary providers for a class
	 *
	 * @param klass The class to get them f or
	 * @return All secondary providers
	 */
	@Nonnull
	<T> Collection<ITransferProvider<? super T>> getSecondaryProviders(Class<T> klass);

	/**
	 * Get the transfer location for an object.
	 *
	 * The algorithm goes as follows:
	 * - Split the string by '.'.
	 * - Fetch the object from the primary part. If {@code null} error.
	 * - For each remaining part lookup using the result of the previous item and the part name.
	 *
	 * @param object The object to get locations from
	 * @param key    The lookup for transfer locations
	 * @return The valid transfer location or {@code null} if none exists.
	 */
	@Nullable
	Object getTransferLocation(@Nonnull Object object, @Nonnull String key);

	/**
	 * Get all primary transfer locations
	 *
	 * @param object The object to get locations from
	 * @return All valid locations
	 */
	@Nonnull
	Set<String> getTransferLocations(@Nonnull Object object);
}
