package org.squiddev.plethora.core.collections;

import com.google.common.collect.Queues;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

/**
 * A iterator which loops through all the subclasses/subinterfaces of an object.
 */
public class ClassIteratorIterable implements Iterable<Class<?>>, Iterator<Class<?>> {
	private final HashSet<Class<?>> visited = new HashSet<>();
	private final Queue<Class<?>> toVisit = Queues.newArrayDeque();

	public ClassIteratorIterable(Class<?> root) {
		visited.add(root);
		toVisit.add(root);
	}


	@Nonnull
	@Override
	public Iterator<Class<?>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return !toVisit.isEmpty();
	}

	@Override
	public Class<?> next() {
		Class<?> klass = toVisit.remove();

		Class<?> parent = klass.getSuperclass();
		if (parent != null && visited.add(parent)) {
			toVisit.add(parent);
		}

		for (Class<?> iface : klass.getInterfaces()) {
			if (iface != null && visited.add(iface)) {
				toVisit.add(iface);
			}
		}

		return klass;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
