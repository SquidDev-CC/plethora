package org.squiddev.plethora.api.converter;

/**
 * A converter which may return a different value for the same input.
 *
 * @see IConverter#isConstant()
 */
public abstract class DynamicConverter<TIn, TOut> implements IConverter<TIn, TOut> {
	@Override
	public boolean isConstant() {
		return false;
	}
}
