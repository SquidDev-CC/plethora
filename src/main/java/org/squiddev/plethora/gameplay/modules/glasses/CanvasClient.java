package org.squiddev.plethora.gameplay.modules.glasses;

import java.util.Map;
import java.util.TreeMap;

public class CanvasClient {
	public final int id;

	public final Map<Integer, BaseObject> objects = new TreeMap<Integer, BaseObject>();

	public CanvasClient(int id) {
		this.id = id;
	}
}
