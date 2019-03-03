package org.squiddev.plethora.api.converter;

import javax.annotation.Nonnull;

/**
 * A registry for implicitly converting objects
 */
public interface IConverterRegistry {
	/**
	 * Convert an object to all convertible objects
	 *
	 * @param in The object to convert from
	 * @return All converted values
	 */
	@Nonnull
	Iterable<?> convertAll(@Nonnull Object in);
}
