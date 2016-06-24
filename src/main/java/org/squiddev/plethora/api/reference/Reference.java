package org.squiddev.plethora.api.reference;

import net.minecraft.tileentity.TileEntity;

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
	public static <T extends TileEntity> IReference<T> tile(T object) {
		return new TileReference<T>(object);
	}
}
