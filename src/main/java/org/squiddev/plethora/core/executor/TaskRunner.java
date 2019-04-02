package org.squiddev.plethora.core.executor;

import net.minecraft.util.ITickable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a system capable of running a series of {@link Task}s.
 *
 * This can be created for a particular object (a tile entity, upgrade, etc...) or one can use the global
 * {@link #SHARED} instance. One should prefer the former, as this ensures execution is associated with that tile
 * (which is helpful for profiling and Forge tracking). However this is not always feasible (such as when you do not
 * have control over the object you are wrapping).
 */
public class TaskRunner implements ITickable {
	public static final TaskRunner SHARED = new TaskRunner();

	private static final int MAX_TASKS_TOTAL = 5000;
	private static final int MAX_TASKS_TICK = 50;

	private final Set<Task> tasks = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Override
	public void update() {
		Iterator<Task> tasks = this.tasks.iterator();

		int i = 0;
		while (tasks.hasNext()) {
			if (tasks.next().update()) tasks.remove();
			if (i++ > MAX_TASKS_TICK) break;
		}
	}

	boolean submit(Task task) {
		return tasks.size() <= MAX_TASKS_TOTAL && tasks.add(task);
	}

	public void reset() {
		tasks.clear();
	}
}
