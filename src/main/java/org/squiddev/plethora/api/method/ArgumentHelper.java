package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

/**
 * Various helpers for arguments.
 *
 * @see dan200.computercraft.core.apis.ArgumentHelper
 */
public final class ArgumentHelper {
	@Nonnull
	public static LuaException badObjectType(@Nonnull String key, @Nonnull String expected, @Nullable Object object) {
		return badObject(key, expected, getType(object));
	}

	@Nonnull
	public static LuaException badObject(@Nonnull String key, @Nonnull String expected, @Nonnull String type) {
		return new LuaException("bad key '" + key + "' (" + expected + " expected, got " + type + ")");
	}

	public static float getFloat(@Nonnull Object[] args, int index) throws LuaException {
		return (float) getReal(args, index);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T extends Enum<T>> T getEnum(@Nonnull Object[] args, int index, Class<T> klass) throws LuaException {
		if (index >= args.length) throw badArgument(index, "number", "no value");
		Object value = args[index];
		if (value instanceof String) {
			String name = (String) value;
			try {
				return Enum.valueOf(klass, name.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e) {
				throw new LuaException("Bad name '" + name.toLowerCase(Locale.ENGLISH) + "' for argument " + (index + 1));
			}
		} else {
			throw badArgument(index, "string", value);
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static UUID getUUID(@Nonnull Object[] args, int index) throws LuaException {
		if (index >= args.length) throw badArgument(index, "number", "no value");
		Object value = args[index];
		if (value instanceof String) {
			String uuid = ((String) value).toLowerCase(Locale.ENGLISH);
			try {
				return UUID.fromString(uuid);
			} catch (IllegalArgumentException e) {
				throw new LuaException("Bad uuid '" + uuid + "' for argument #" + (index + 1));
			}
		} else {
			throw badArgument(index, "string", value);
		}
	}


	public static float optFloat(@Nonnull Object[] args, int index, float def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Number) {
			return ((Number) value).floatValue();
		} else {
			throw badArgument(index, "number", value);
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T extends Enum<T>> T optEnum(@Nonnull Object[] args, int index, Class<T> klass, T def) throws LuaException {
		if (index >= args.length || args[index] == null) {
			return def;
		} else {
			return getEnum(args, index, klass);
		}
	}

	public static void assertBetween(double value, double min, double max, String message) throws LuaException {
		if (value < min || value > max || Double.isNaN(value)) {
			throw new LuaException(String.format(message, "between " + min + " and " + max));
		}
	}

	public static void assertBetween(int value, int min, int max, String message) throws LuaException {
		if (value < min || value > max) {
			throw new LuaException(String.format(message, "between " + min + " and " + max));
		}
	}

	public static String numberType(double value) {
		if (Double.isNaN(value)) {
			return "nan";
		} else if (value == Double.POSITIVE_INFINITY) {
			return "inf";
		} else if (value == Double.NEGATIVE_INFINITY) {
			return "-inf";
		} else {
			return "number";
		}
	}
}
