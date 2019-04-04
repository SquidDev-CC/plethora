package org.squiddev.plethora.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

/**
 * A simple wrapper class for constructing Lua lists. Not thread safe!
 */
public class LuaList<T> {
	/**
	 * Creates a {@link Collector} that produces a LuaList
	 */
	public static <T> Collector<T, ?, LuaList<T>> toLuaList() {
		return Collector.of(
			LuaList::new,
			LuaList::add,
			(a, b) -> {
				b.map.values().forEach(a::add);
				return a;
			},
			Collector.Characteristics.IDENTITY_FINISH
		);
	}

	private final Map<Integer, T> map;
	private int lastIndex = 0;

	/**
	 * Creates a new, empty list
	 */
	public LuaList() {
		map = new HashMap<>();
	}

	/**
	 * Creates a new, empty list
	 *
	 * @param expectedSize The expected size of this list
	 */
	public LuaList(int expectedSize) {
		map = new HashMap<>(expectedSize);
	}

	/**
	 * Creates a new list using elements from the given {@link Collection}
	 */
	public LuaList(Collection<T> of) {
		this(of.size());
		addAll(of);
	}

	/**
	 * Adds an element to the end of the list
	 */
	public void add(T e) {
		map.put(++lastIndex, e);
	}

	/**
	 * Adds all elements from the given {@link Iterable}
	 */
	public void addAll(Iterable<T> from) {
		from.forEach(this::add);
	}

	/**
	 * Determine if this list is empty
	 *
	 * @return If this list is empty
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns this list as a {@link Map} of 1-indexed integer keys to values
	 */
	public Map<Integer, T> asMap() {
		return map;
	}
}
