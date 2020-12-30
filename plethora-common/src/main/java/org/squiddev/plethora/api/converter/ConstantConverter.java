package org.squiddev.plethora.api.converter;

/**
 * A converter which always returns the same value (or a value which is equivalent).
 *
 * @see IConverter#isConstant()
 */
@FunctionalInterface
public interface ConstantConverter<TIn, TOut> extends IConverter<TIn, TOut> {
	@Override
	default boolean isConstant() {
		return true;
	}
}
