package org.squiddev.plethora.gameplay.modules.glasses;

import it.unimi.dsi.fastutil.ints.*;
import org.squiddev.plethora.utils.DebugLogger;

public class CanvasClient {
	public final int id;

	private final Int2ObjectMap<BaseObject> objects = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<IntSortedSet> childrenOf = new Int2ObjectOpenHashMap<>();

	public CanvasClient(int id) {
		this.id = id;
		this.childrenOf.put(0, new IntAVLTreeSet());
	}

	public void updateObject(BaseObject object) {
		IntSet parent = childrenOf.get(object.parent());
		if (parent == null) {
			DebugLogger.error("No children for " + object + " (" + object.parent() + ")");
			return; // Should never happen but...
		}

		if (objects.put(object.id(), object) == null) {
			// If this is a new instance then setup the children
			parent.add(object.id());
			if (object instanceof ObjectGroup) childrenOf.put(object.id(), new IntAVLTreeSet());
		}
	}

	public void remove(int id) {
		BaseObject object = objects.remove(id);
		childrenOf.remove(id); // We handle the removing of children in the server version

		if (object != null) {
			// Remove from the parent set if needed.
			IntSet parent = childrenOf.get(object.parent());
			if (parent != null) parent.remove(id);
		}
	}

	public BaseObject getObject(int id) {
		return objects.get(id);
	}

	public IntSet getChildren(int id) {
		return childrenOf.get(id);
	}
}
