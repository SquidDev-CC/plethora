package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CanvasServer extends ConstantReference<CanvasServer> {
	private final int canvasId;
	private final IModuleAccess access;
	private final EntityPlayerMP player;

	private final Int2ObjectMap<BaseObject> objects = new Int2ObjectOpenHashMap<>();
	private int lastId = 0;

	private final IntSet removed = new IntOpenHashSet();

	public CanvasServer(int canvasId, @Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvasId = canvasId;
		this.access = access;
		this.player = player;
	}

	public void attach() {
		access.getData().setInteger("id", canvasId);
		access.markDataDirty();
	}

	public void detach() {
		access.getData().removeTag("id");
		access.markDataDirty();
	}

	public synchronized int newObjectId() {
		return lastId++;
	}

	@Nonnull
	public synchronized MessageCanvasAdd getAddMessage() {
		return new MessageCanvasAdd(canvasId, objects.values().toArray(new BaseObject[objects.size()]));
	}

	@Nonnull
	public MessageCanvasRemove getRemoveMessage() {
		return new MessageCanvasRemove(canvasId);
	}

	@Nullable
	public synchronized MessageCanvasUpdate getUpdateMessage() {
		List<BaseObject> changed = null;
		for (BaseObject object : objects.values()) {
			if (object.pollDirty()) {
				if (changed == null) changed = new ArrayList<>();
				changed.add(object);
			}
		}

		if (changed == null && removed.size() == 0) return null;

		if (changed == null) changed = Collections.emptyList();
		MessageCanvasUpdate message = new MessageCanvasUpdate(
			canvasId, changed, removed.toIntArray()
		);

		removed.clear();

		return message;
	}

	public synchronized void add(BaseObject object) {
		if (objects.put(object.id, object) != null) {
			throw new IllegalStateException("An object already exists with that key");
		}
	}

	public synchronized void remove(BaseObject object) {
		if (objects.remove(object.id) == null) {
			throw new IllegalStateException("No such object with this key");
		}

		removed.add(object.id);
	}

	public synchronized BaseObject getObject(int id) {
		return objects.get(id);
	}

	public synchronized void clear() {
		for (BaseObject object : objects.values()) {
			removed.add(object.id);
		}
		objects.clear();
	}

	@Nonnull
	public EntityPlayerMP getPlayer() {
		return player;
	}

	@Nonnull
	@Override
	public CanvasServer get() throws LuaException {
		return this;
	}

	@Nonnull
	@Override
	public CanvasServer safeGet() throws LuaException {
		return this;
	}
}
