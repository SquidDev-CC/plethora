package org.squiddev.plethora.api.reference;

/**
 * A reference which always returns the same value (or a value which is equivalent).
 *
 * @see IReference#isConstant()
 */
public abstract class DynamicReference<T> implements IReference<T> {
	@Override
	public boolean isConstant() {
		return false;
	}
}
