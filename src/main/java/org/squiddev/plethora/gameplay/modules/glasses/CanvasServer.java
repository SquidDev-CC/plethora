package org.squiddev.plethora.gameplay.modules.glasses;

import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

public class CanvasServer implements IReference<CanvasServer> {
	private final int canvasId;
	private final IModuleAccess access;
	private final EntityPlayerMP player;

	private final TIntObjectHashMap<BaseObject> objects = new TIntObjectHashMap<BaseObject>();
	private int lastId = 0;

	private final Set<BaseObject> added = Sets.newHashSet();
	private final TIntHashSet removed = new TIntHashSet();

	public CanvasServer(int canvasId, @Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvasId = canvasId;
		this.access = access;
		this.player = player;

		access.getData().setInteger("id", canvasId);
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
		ArrayList<BaseObject> changed = null;
		for (BaseObject object : objects.valueCollection()) {
			if (object.isDirty()) {
				if (changed == null) changed = new ArrayList<BaseObject>();
				changed.add(object);
			}
		}

		if (changed == null && added.size() == 0 && removed.size() == 0) return null;

		if (changed == null) changed = new ArrayList<BaseObject>(0);
		MessageCanvasUpdate message = new MessageCanvasUpdate(
			canvasId, changed, new ArrayList<BaseObject>(added), removed.toArray()
		);
		added.clear();
		removed.clear();

		return message;
	}

	public synchronized void add(BaseObject object) {
		if (objects.put(object.id, object) != null) {
			throw new IllegalStateException("An object already exists with that key");
		}

		object.resetDirty();
		added.add(object);
	}

	public synchronized void remove(BaseObject object) {
		if (objects.remove(object.id) == null) {
			throw new IllegalStateException("No such object with this key");
		}

		if (!added.remove(object)) removed.add(object.id);
	}

	public synchronized BaseObject getObject(int id) {
		return objects.get(id);
	}

	public synchronized void clear() {
		for (BaseObject object : objects.valueCollection()) {
			if (!added.remove(object)) removed.add(object.id);
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
