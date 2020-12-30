package org.squiddev.plethora.api.converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An implicit converter: takes an object and converts it into something else
 */
public interface IConverter<TIn, TOut> {
	/**
	 * Convert an object from one object into another.
	 * Used to provide additional objects from one
	 *
	 * @param from The object to convert from
	 * @return The converted object
	 */
	@Nullable
	TOut convert(@Nonnull TIn from);

	/**
	 * Whether this converter will always return the same object for a given
	 * input.
	 *
	 * Note, you may make some assumptions about the conversion,
	 * such that the block and tile will not change.
	 *
	 * @return If this converter is constant.
	 * @see ConstantConverter
	 * @see DynamicConverter
	 */
	boolean isConstant();
}
