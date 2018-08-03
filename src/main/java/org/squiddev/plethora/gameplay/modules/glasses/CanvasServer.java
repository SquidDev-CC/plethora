package org.squiddev.plethora.gameplay.modules.glasses;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.gameplay.modules.glasses.objects.ObjectGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.ID_2D;
import static org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler.ID_3D;

public final class CanvasServer extends ConstantReference<CanvasServer> implements IAttachable {
	private final int canvasId;
	private final IModuleAccess access;
	private final EntityPlayerMP player;

	private final Int2ObjectMap<BaseObject> objects = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<IntSet> childrenOf = new Int2ObjectOpenHashMap<>();

	private final IntSet removed = new IntOpenHashSet();

	private AtomicInteger lastId = new AtomicInteger(ID_3D);

	private final ObjectGroup.Frame2D group2D = () -> ID_2D;
	private final ObjectGroup.Origin3D origin3D = () -> ID_3D;

	public CanvasServer(@Nonnull IModuleAccess access, @Nonnull EntityPlayerMP player) {
		this.canvasId = CanvasHandler.nextId();
		this.access = access;
		this.player = player;

		this.childrenOf.put(ID_2D, new IntOpenHashSet());
		this.childrenOf.put(ID_3D, new IntOpenHashSet());
	}

	@Override
	public void attach() {
		access.getData().setInteger("id", canvasId);
		access.markDataDirty();
		CanvasHandler.addServer(this);
	}

	@Override
	public void detach() {
		CanvasHandler.removeServer(this);
		access.getData().removeTag("id");
		access.markDataDirty();
	}

	public int newObjectId() {
		return lastId.incrementAndGet();
	}

	public ObjectGroup.Frame2D canvas2d() {
		return group2D;
	}

	public ObjectGroup.Origin3D canvas3d() {
		return origin3D;
	}

	@Nonnull
	synchronized MessageCanvasAdd getAddMessage() {
		return new MessageCanvasAdd(canvasId, objects.values().toArray(new BaseObject[objects.size()]));
	}

	@Nonnull
	MessageCanvasRemove getRemoveMessage() {
		return new MessageCanvasRemove(canvasId);
	}

	@Nullable
	synchronized MessageCanvasUpdate getUpdateMessage() {
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

	public synchronized void add(@Nonnull BaseObject object) {
		IntSet parent = childrenOf.get(object.parent());
		if (parent == null) throw new IllegalArgumentException("No such parent");

		if (objects.put(object.id(), object) != null) {
			throw new IllegalStateException("An object already exists with that key");
		}

		parent.add(object.id());
		if (object instanceof ObjectGroup) childrenOf.put(object.id(), new IntOpenHashSet());
	}

	public synchronized void remove(BaseObject object) {
		if (!removeImpl(object.id())) {
			throw new IllegalStateException("No such object with this key");
		}
	}

	public synchronized BaseObject getObject(int id) {
		return objects.get(id);
	}

	public synchronized void clear(ObjectGroup object) {
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
