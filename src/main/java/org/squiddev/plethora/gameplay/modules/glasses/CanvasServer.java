package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
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

	private final TIntObjectHashMap<BaseObject> objects = new TIntObjectHashMap<>();
	private int lastId = 0;

	private final TIntHashSet removed = new TIntHashSet();

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
		return new MessageCanvasAdd(canvasId, objects.values(new BaseObject[objects.size()]));
	}

	@Nonnull
	public MessageCanvasRemove getRemoveMessage() {
		return new MessageCanvasRemove(canvasId);
	}

	@Nullable
	public synchronized MessageCanvasUpdate getUpdateMessage() {
		List<BaseObject> changed = null;
		for (BaseObject object : objects.valueCollection()) {
			if (object.pollDirty()) {
				if (changed == null) changed = new ArrayList<>();
				changed.add(object);
			}
		}

		if (changed == null && removed.size() == 0) return null;

		if (changed == null) changed = Collections.emptyList();
		MessageCanvasUpdate message = new MessageCanvasUpdate(
			canvasId, changed, removed.toArray()
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
		for (BaseObject object : objects.valueCollection()) {
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
