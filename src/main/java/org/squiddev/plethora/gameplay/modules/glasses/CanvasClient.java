package org.squiddev.plethora.gameplay.modules.glasses;

import gnu.trove.map.hash.TIntObjectHashMap;

public class CanvasClient {
	public final int id;

	public final TIntObjectHashMap<BaseObject> objects = new TIntObjectHashMap<BaseObject>();

	public CanvasClient(int id) {
		this.id = id;
	}
}
