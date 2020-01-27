package org.squiddev.plethora.core;

import com.google.common.collect.Multimap;
import org.squiddev.plethora.core.collections.SortedMultimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link RegisteredValue} which holds a single target class and value.
 *
 * @param <T> The type of this registration entry.
 */
class TargetedRegisteredValue<T> extends RegisteredValue {
	private final Class<?> target;
	private final T value;

	TargetedRegisteredValue(@Nonnull String name, @Nullable String mod, @Nonnull Class<?> target, @Nonnull T value) {
		super(name, mod);
		this.target = target;
		this.value = value;
	}

	@Nonnull
	final Class<?> target() {
		return target;
	}

	@Nonnull
	final T value() {
		return value;
	}

	static <T> void buildCache(Iterable<TargetedRegisteredValue<? extends T>> all, Multimap<Class<?>, T> cache) {
		cache.clear();
		for (TargetedRegisteredValue<? extends T> item : all) {
			if (item.enabled()) cache.put(item.target(), item.value());
		}
	}

	static <T> void buildCache(Iterable<TargetedRegisteredValue<? extends T>> all, SortedMultimap<Class<?>, T> cache) {
		cache.clear();
		for (TargetedRegisteredValue<? extends T> item : all) {
			if (item.enabled()) cache.put(item.target(), item.value());
		}
	}
}
