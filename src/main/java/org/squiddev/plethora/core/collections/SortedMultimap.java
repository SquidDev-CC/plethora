package org.squiddev.plethora.core.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SortedMultimap<K, V> {
	private final Comparator<V> comparator;
	private final HashMap<K, SortedCollection<V>> items = new HashMap<K, SortedCollection<V>>();

	public SortedMultimap(Comparator<V> comparator) {
		this.comparator = comparator;
	}

	public void put(K key, V value) {
		SortedCollection<V> targetItems = items.get(key);
		if (targetItems == null) {
			targetItems = SortedCollection.create(comparator);
			items.put(key, targetItems);
		}

		targetItems.add(value);
	}

	public Collection<V> get(K key) {
		Collection<V> result = items.get(key);
		if (result == null) {
			return Collections.emptyList();
		} else {
			return result;
		}
	}

	public static <K, V> SortedMultimap<K, V> create(Comparator<V> comparator) {
		return new SortedMultimap<K, V>(comparator);
	}
}
