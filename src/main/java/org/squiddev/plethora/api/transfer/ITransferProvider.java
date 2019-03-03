package org.squiddev.plethora.api.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * A provider for a transfer location
 *
 * A "transfer location" is somewhere where objects can be transferred to or from. For instance you might target
 * {@link net.minecraft.entity.player.EntityPlayer} which could provide "inventory" and "ender_inventory".
 *
 * Transfer locations can also be chained together: each item is separated by ".". For instance "inventory.2" would
 * look-up "inventory" then lookup "2" on the resulting inventory object.
 *
 * The first entry is known as a "primary" location, the others as secondary. You can use {@link #primary()} and
 * {@link #secondary()} to mark this provider as only applying in one case. This may be useful if you want to hide
 * locations showing up without {@code "self."}, or if you do not want people to use it multiple times
 * (say {@code "north.north.north"} to allow indexing arbitrary positions).
 */
public interface ITransferProvider<T> {
	/**
	 * @param object The object to get locations from
	 * @param key    The lookup for transfer locations
	 * @return The valid transfer location or {@code null} if none exists.
	 */
	@Nullable
	Object getTransferLocation(@Nonnull T object, @Nonnull String key);

	/**
	 * Get all transfer locations
	 *
	 * @param object The object to get locations from
	 * @return All valid locations.
	 */
	@Nonnull
	Set<String> getTransferLocations(@Nonnull T object);

	/**
	 * Whether this converter is a primary converter
	 *
	 * @return If this is a primary converter
	 */
	default boolean primary() {
		return true;
	}

	/**
	 * Whether this converter is a secondary converter
	 *
	 * @return If this is a secondary converter
	 */
	default boolean secondary() {
		return true;
	}
}
