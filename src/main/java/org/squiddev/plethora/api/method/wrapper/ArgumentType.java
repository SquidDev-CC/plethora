package org.squiddev.plethora.api.method.wrapper;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface ArgumentType<T> {
	String name();

	@Nonnull
	T get(@Nonnull Object[] args, int index) throws LuaException;

	@Nullable
	default T opt(@Nonnull Object[] args, int index) throws LuaException {
		return index < args.length && args[index] != null ? get(args, index) : null;
	}

	default <U> ArgumentType<U> map(ArgumentFunction<T, U> func) {
		Objects.requireNonNull(func);

		ArgumentType<T> original = this;
		return new ArgumentType<U>() {
			@Override
			public String name() {
				return original.name();
			}

			@Nonnull
			@Override
			public U get(@Nonnull Object[] args, int index) throws LuaException {
				return func.apply(original.get(args, index));
			}

			@Nullable
			@Override
			public U opt(@Nonnull Object[] args, int index) throws LuaException {
				T result = original.opt(args, index);
				return result == null ? null : func.apply(result);
			}

			@Override
			public <V> ArgumentType<V> map(ArgumentFunction<U, V> func2) {
				Objects.requireNonNull(func2);
				return original.map(x -> func2.apply(func.apply(x)));
			}
		};
	}

	interface ArgumentGetter<T> {
		@Nonnull
		T get(@Nonnull Object[] args, int index) throws LuaException;
	}

	interface ArgumentFunction<T, U> {
		@Nonnull
		U apply(@Nonnull T value) throws LuaException;
	}
}
