package org.squiddev.plethora.api.reference;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * An reference to an entity. Ensures it is still alive.
 */
public class EntityReference<T extends Entity> extends ConstantReference<T> {
	private final MinecraftServer server;
	private final UUID id;
	private WeakReference<T> entity;

	EntityReference(T entity) {
		this.server = entity.getServer();
		this.id = entity.getUniqueID();
		this.entity = new WeakReference<>(entity);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	@Override
	public T get() throws LuaException {
		T entity = this.entity.get();

		if (entity == null || entity.isDead || entity.getEntityWorld().getEntityByID(entity.getEntityId()) != entity) {
			entity = (T) server.getEntityFromUuid(id);
			if (entity == null || entity.isDead) throw new LuaException("The entity is no longer there");

			this.entity = new WeakReference<>(entity);
		}

		return entity;
	}

	@Nonnull
	@Override
	public T safeGet() throws LuaException {
		T value = entity.get();
		if (value == null || value.isDead) throw new LuaException("The entity is no longer there");

		return value;
	}
}
