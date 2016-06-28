package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;

/**
 * A reference that returns its value
 */
public final class IdentityReference<T> implements IReference<T> {
	private final T object;

	public IdentityReference(@Nonnull T object) {
		this.object = object;
	}

	@Nonnull
	@Override
	public T get() throws LuaException {
		return object;
	}
}
