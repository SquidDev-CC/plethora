package org.squiddev.plethora.api.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A simple helper class for constructing lists of Lua values.
 */
public class LuaList<T> {
	private static final Collector<Object, ?, LuaList<Object>> COLLECTOR = Collector.of(
		LuaList::new,
		LuaList::add,
		(a, b) -> {
			for (Object t : b.map.values()) a.add(t);
			return a;
		},
		Collector.Characteristics.IDENTITY_FINISH
	);

	/**
	 * Creates a {@link Collector} that produces a {@link LuaList}.
	 * Prefer {@link LuaList#of(Collection, Function)}
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> Collector<T, ?, LuaList<T>> toLuaList() {
		return (Collector) COLLECTOR;
	}

	public static <T, R> LuaList<R> of(Collection<T> items, Function<T, R> map) {
		LuaList<R> list = new LuaList<>(items.size());
		for (T item : items) list.add(map.apply(item));
		return list;
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
	 * Create a new list using elements from the given array
	 *
	 * @param array The array to construct from.
	 */
	public LuaList(T[] array) {
		this(array.length);
		for (int i = 0; i < array.length; i++) map.put(i + 1, array[i]);
		lastIndex = array.length;
	}

	/**
	 * Adds an element to the end of the list
	 */
	public void add(T e) {
		int i = ++lastIndex;
		if (e != null) map.put(i, e);
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
