package org.squiddev.plethora.api.reference;

import javax.annotation.Nonnull;

/**
 * A reference that returns its value
 */
public final class IdentityReference<T> implements ConstantReference<T> {
	private final T object;

	IdentityReference(@Nonnull T object) {
		this.object = object;
	}

	@Nonnull
	@Override
	public T get() {
		return object;
	}

	@Nonnull
	@Override
	public T safeGet() {
		return object;
	}
}
