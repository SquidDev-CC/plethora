package org.squiddev.plethora.gameplay.modules;

import com.google.common.collect.Sets;
import org.squiddev.plethora.gameplay.modules.glasses.GlassesInstance;

import java.util.HashSet;

public class GlassesHandler {
	private static final HashSet<GlassesInstance> active = Sets.newHashSet();

	public static void add(GlassesInstance instance) {
		synchronized (active) {
			active.add(instance);
		}
	}

	public static void remove(GlassesInstance instance) {
		synchronized (active) {
			active.remove(instance);
		}
	}

	public static void clear() {
		synchronized (active) {
			active.clear();
		}
	}
}
