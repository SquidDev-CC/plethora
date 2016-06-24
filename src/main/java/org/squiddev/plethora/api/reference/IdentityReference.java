package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;

/**
 * A reference that returns its value
 */
public class IdentityReference<T> implements IReference<T> {
	private final T object;

	public IdentityReference(T object) {
		this.object = object;
	}

	@Override
	public T get() throws LuaException {
		return object;
	}
}
