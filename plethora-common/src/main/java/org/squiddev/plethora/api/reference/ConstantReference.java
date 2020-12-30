package org.squiddev.plethora.api.reference;

/**
 * A reference which always returns the same value (or a value which is equivalent).
 *
 * @see IReference#isConstant()
 */
public interface ConstantReference<T> extends IReference<T> {
	@Override
	default boolean isConstant() {
		return true;
	}
}
