package org.squiddev.plethora.utils;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Collector;

public class LuaList<T> {
	/**
	 * Creates a {@link Collector} that produces a LuaList
	 */
	public static <T>Collector<T, ?, LuaList<T>> toLuaList() {
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

	public LuaList() {
		map = Maps.newHashMap();
	}

	/**
	 * Creates a new, empty LuaList
	 * @param expectedSize The expected size of this list
	 */
	public LuaList(int expectedSize) {
		map = Maps.newHashMapWithExpectedSize(expectedSize);
	}

	/**
	 * Adds an element to the end of the list
	 */
	public void add(T e) {
		map.put(++lastIndex, e);
	}

	/**
	 * Converts the list to a map of 1-indexed integer keys to values
	 */
	public Map<Integer, T> asMap() {
		return map;
	}
}
