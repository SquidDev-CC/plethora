package org.squiddev.plethora.api.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * The primary registry for transfer providers
 */
public interface ITransferRegistry {
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
	 * Get a transfer location for a single "part" of the path
	 *
	 * @param object    The object to get locations from
	 * @param part      The lookup for transfer locations
	 * @param secondary Use the secondary transfer providers
	 * @return The valid transfer location or {@code null} if none exists.
	 */
	@Nullable
	Object getTransferPart(@Nonnull Object object, @Nonnull String part, boolean secondary);

	/**
	 * Get all primary transfer locations
	 *
	 * @param object The object to get locations from
	 * @return All valid locations
	 */
	@Nonnull
	Set<String> getTransferLocations(@Nonnull Object object, boolean primary);
}
