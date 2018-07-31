package org.squiddev.plethora.gameplay.modules.glasses;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class CanvasServer extends ConstantReference<CanvasServer> implements IObjectGroup {
	private final int canvasId;
	private final IModuleAccess access;
	private final EntityPlayerMP player;

	private final Int2ObjectMap<BaseObject> objects = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<IntSet> childrenOf = new Int2ObjectOpenHashMap<>();

	private AtomicInteger lastId = new AtomicInteger(0);

	private final IntSet removed = new IntOpenHashSet();

	public CanvasServer(int canvasId, @Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvasId = canvasId;
		this.access = access;
		this.player = player;

		this.childrenOf.put(0, new IntOpenHashSet());
	}

	public void attach() {
		access.getData().setInteger("id", canvasId);
		access.markDataDirty();
	}

	public void detach() {
		access.getData().removeTag("id");
		access.markDataDirty();
	}

	public int newObjectId() {
		return lastId.incrementAndGet();
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

	@Override
	public int id() {
		return 0;
	}

	public synchronized void add(@Nonnull BaseObject object) {
		IntSet parent = childrenOf.get(object.parent());
		if (parent == null) throw new IllegalArgumentException("No such parent");

		if (objects.put(object.id(), object) != null) {
			throw new IllegalStateException("An object already exists with that key");
		}

		parent.add(object.id());
		if (object instanceof IObjectGroup) childrenOf.put(object.id(), new IntOpenHashSet());
	}

	public synchronized void remove(BaseObject object) {
		if (!removeImpl(object.id())) {
			throw new IllegalStateException("No such object with this key");
		}
	}

	public synchronized BaseObject getObject(int id) {
		return objects.get(id);
	}

	public synchronized void clear(IObjectGroup object) {
		IntSet children = this.childrenOf.get(object.id());
		if (children == null) throw new IllegalStateException("Object has no children");

		clearImpl(children);
	}

	private boolean removeImpl(int id) {
		if (objects.remove(id) == null) return false;

		IntSet children = childrenOf.remove(id);
		if (children != null) clearImpl(children);

		removed.add(id);
		return true;
	}

	private void clearImpl(IntSet objects) {
		for (IntIterator iterator = objects.iterator(); iterator.hasNext(); ) {
			int childId = iterator.nextInt();
			removeImpl(childId);
			iterator.remove();
		}
	}

	@Nonnull
	public EntityPlayerMP getPlayer() {
		return player;
	}

	@Nonnull
	@Override
	public CanvasServer get() {
		return this;
	}

	@Nonnull
	@Override
	public CanvasServer safeGet() {
		return this;
	}
}
