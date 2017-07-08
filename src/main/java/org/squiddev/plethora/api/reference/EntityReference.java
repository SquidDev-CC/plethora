package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

/**
 * An reference to an entity. Ensures it is still alive.
 */
public class EntityReference<T extends Entity> implements IReference<T> {
	private final WeakReference<T> entity;

	public EntityReference(T entity) {
		this.entity = new WeakReference<>(entity);
	}

	@Nonnull
	@Override
	public T get() throws LuaException {
		T value = entity.get();
		if (value == null || value.isDead) throw new LuaException("The entity is no longer there");

		return value;
	}

	@Nonnull
	@Override
	public T safeGet() throws LuaException {
		T value = entity.get();
		if (value == null || value.isDead) throw new LuaException("The entity is no longer there");

		return value;
	}
}
