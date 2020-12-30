package org.squiddev.plethora.api.converter;

/**
 * A converter which may return a different value for the same input.
 *
 * @see IConverter#isConstant()
 */
@FunctionalInterface
public interface DynamicConverter<TIn, TOut> extends IConverter<TIn, TOut> {
	@Override
	default boolean isConstant() {
		return false;
	}
}
