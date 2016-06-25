package org.squiddev.plethora.api.converter;

import java.util.List;

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
	<TIn, TOut> void registerConverter(Class<TIn> source, IConverter<TIn, TOut> converter);

	/**
	 * Convert an object to all convertable objects
	 *
	 * @param in The object to convert from
	 * @return All converted values
	 */
	List<?> convertAll(Object in);
}
