package org.squiddev.plethora.api.converter;

/**
 * A converter which always returns the same value (or a value which is equivalent).
 *
 * @see IConverter#isConstant()
 */
public abstract class ConstantConverter<TIn, TOut> implements IConverter<TIn, TOut> {
	@Override
	public boolean isConstant() {
		return true;
	}
}
