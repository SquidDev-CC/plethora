package org.squiddev.plethora.api.converter;

import javax.annotation.Nonnull;

/**
 * A registry for implicitly converting objects
 */
public interface IConverterRegistry {
	/**
	 * Register a converter
	 *
	 * @param source    The type to convert from
	 * @param converter The converter method
	 */
	<TIn, TOut> void registerConverter(@Nonnull Class<TIn> source, @Nonnull IConverter<TIn, TOut> converter);

	/**
	 * Convert an object to all convertible objects
	 *
	 * @param in The object to convert from
	 * @return All converted values
	 */
	@Nonnull
	Iterable<?> convertAll(@Nonnull Object in);
}
