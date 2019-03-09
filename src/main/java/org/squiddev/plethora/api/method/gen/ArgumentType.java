package org.squiddev.plethora.api.method.gen;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@FunctionalInterface
public interface ArgumentType<T> {
	@Nonnull
	T get(@Nonnull Object[] args, int index) throws LuaException;

	@Nullable
	default T opt(@Nonnull Object[] args, int index) throws LuaException {
		return index < args.length && args[index] != null ? get(args, index) : null;
	}

	default <U> ArgumentType<U> map(LuaFunction<T, U> func) {
		Objects.requireNonNull(func);

		ArgumentType<T> original = this;
		return new ArgumentType<U>() {
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
			public <V> ArgumentType<V> map(LuaFunction<U, V> func2) {
				Objects.requireNonNull(func2);
				return original.map(x -> func2.apply(func.apply(x)));
			}
		};
	}

	interface LuaFunction<T, U> {
		@Nonnull
		U apply(@Nonnull T value) throws LuaException;
	}
}
