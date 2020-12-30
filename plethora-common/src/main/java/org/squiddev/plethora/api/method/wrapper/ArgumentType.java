package org.squiddev.plethora.api.method.wrapper;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A converter and validator from a Lua object to a standard Java type.
 *
 * @param <T> The type to which we will convert
 * @see org.squiddev.plethora.api.Injects For how to register this.
 * @see PlethoraMethod For how to use this
 * @see dan200.computercraft.api.peripheral.IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[]) for
 * the sort of values you should validate against.
 * @see ArgumentTypes
 * @see org.squiddev.plethora.api.method.ArgumentHelper
 */
public interface ArgumentType<T> {
	/**
	 * The friendly name of this argument type, used in signatures.
	 *
	 * Normally this can be the underlying representation of the type, such as {@code string} or {@code number|table}.
	 *
	 * @return This argument type's name.
	 */
	String name();

	/**
	 * Require this type from the given argument list
	 *
	 * @param args  All arguments to this function
	 * @param index The index at which to extract the argument. Note, this may be beyond the end of {@code args}.
	 * @return The converted value
	 * @throws LuaException If the value is not valid, or was not provided at all.
	 */
	@Nonnull
	T get(@Nonnull Object[] args, int index) throws LuaException;

	/**
	 * Attempt to extract this type from the given argument list if present.
	 *
	 * @param args  All arguments to this function
	 * @param index The index at which to extract the argument. Note, this may be beyond the end of {@code args}.
	 * @return The converted value, or {@code null} if not provided.
	 * @throws LuaException If the value is not valid.
	 */
	@Nullable
	default T opt(@Nonnull Object[] args, int index) throws LuaException {
		return index < args.length && args[index] != null ? get(args, index) : null;
	}

	/**
	 * Convert the result of this argument type to another
	 *
	 * @param func The conversion function.
	 * @param <U>  The type to convert to.
	 * @return The new argument type.
	 */
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

	/**
	 * A conversion function for {@link #map(ArgumentFunction)}
	 *
	 * @param <T> The type to convert from.
	 * @param <U> The type to convert to.
	 */
	@FunctionalInterface
	interface ArgumentFunction<T, U> {
		@Nonnull
		U apply(@Nonnull T value) throws LuaException;
	}
}
