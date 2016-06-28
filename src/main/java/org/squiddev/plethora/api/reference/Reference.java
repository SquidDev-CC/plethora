package org.squiddev.plethora.api.reference;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.WorldLocation;

import javax.annotation.Nonnull;

/**
 * Factory for various reference types
 */
public final class Reference {
	private Reference() {
		throw new IllegalStateException("Cannot create a Reference instance");
	}

	/**
	 * Create an identity reference
	 *
	 * @param object The object to wrap
	 * @return The wrapped reference
	 * @see IdentityReference
	 */
	@Nonnull
	public static <T> IReference<T> id(T object) {
		return new IdentityReference<T>(object);
	}

	/**
	 * Create an reference to a {@link TileEntity}
	 *
	 * @param object The tile to wrap
	 * @return The wrapped reference
	 * @see TileReference
	 */
	@Nonnull
	public static <T extends TileEntity> IReference<T> tile(T object) {
		return new TileReference<T>(object);
	}

	/**
	 * Create an reference to a {@link Entity}
	 *
	 * @param object The entity to wrap
	 * @return The wrapped reference
	 * @see EntityReference
	 */
	@Nonnull
	public static <T extends Entity> IReference<T> entity(T object) {
		return new EntityReference<T>(object);
	}

	/**
	 * Create a reference to a {@link Entity} that must be within a radius of a block
	 *
	 * @param object   The entity to wrap
	 * @param location The location to check around
	 * @param radius   The radius of this size
	 * @return The wrapped reference
	 * @see BoundedEntityReference
	 */
	@Nonnull
	public static <T extends Entity> IReference<T> bounded(T object, WorldLocation location, int radius) {
		return new BoundedEntityReference<T>(object, location, radius);
	}
}
