package org.squiddev.plethora.core.collections;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A basic sorted collection.
 * This should be more efficient than a {@link TreeSet}
 */
public class SortedCollection<E> implements Collection<E> {
	private final ArrayList<E> list = new ArrayList<>();
	private boolean sorted = true;

	private final Comparator<? super E> comparator;

	public SortedCollection(Comparator<? super E> comparator) {
		this.comparator = comparator;
	}

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		if (!sorted) {
			list.sort(comparator);
			sorted = true;
		}

		return list.iterator();
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Nonnull
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <T> T[] toArray(@Nonnull T[] array) {
		return list.toArray(array);
	}

	@Override
	public boolean add(E t) {
		if (list.add(t)) {
			sorted = false;
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(@Nonnull Collection<?> collection) {
		return list.containsAll(collection);
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends E> collection) {
		if (list.addAll(collection)) {
			sorted = false;
			return true;
		}

		return false;
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> collection) {
		return list.removeAll(collection);
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> collection) {
		return list.retainAll(collection);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	public static <V> SortedCollection<V> create(Comparator<V> comparator) {
		return new SortedCollection<>(comparator);
	}
}
