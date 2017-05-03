package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Various helpers for arguments
 */
public final class ArgumentHelper {
	private ArgumentHelper() {
		throw new IllegalStateException("Cannot instantiate singleton " + getClass().getName());
	}

	@Nonnull
	public static String getType(@Nullable Object type) {
		if (type == null) return "nil";
		if (type instanceof String) return "string";
		if (type instanceof Boolean) return "boolean";
		if (type instanceof Number) return "number";
		if (type instanceof Map) return "table";

		Class<?> klass = type.getClass();
		if (klass.isArray()) {
			StringBuilder name = new StringBuilder();
			while (klass.isArray()) {
				name.append("[]");
				klass = klass.getComponentType();
			}
			name.insert(0, klass.getName());
			return name.toString();
		} else {
			return klass.getName();
		}
	}

	@Nonnull
	public static LuaException badArgument(@Nullable Object object, int index, @Nonnull String expected) {
		return badArgument(index, expected, getType(object));
	}

	@Nonnull
	public static LuaException badArgument(int index, @Nonnull String expected, @Nonnull String got) {
		return new LuaException("Expected " + expected + " for argument " + (index + 1) + ", got " + got);
	}

	public static double getNumber(@Nonnull Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			throw badArgument(value, index, "number");
		}
	}

	public static int getInt(@Nonnull Object[] args, int index) throws LuaException {
		return (int) getNumber(args, index);
	}

	public static double getReal(@Nonnull Object[] args, int index) throws LuaException {
		return checkReal(index, getNumber(args, index));
	}

	public static boolean getBoolean(@Nonnull Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			throw badArgument(value, index, "boolean");
		}
	}

	@Nonnull
	public static String getString(@Nonnull Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof String) {
			return (String) value;
		} else {
			throw badArgument(value, index, "string");
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static Map<Object, Object> getTable(@Nonnull Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Map) {
			return (Map<Object, Object>) value;
		} else {
			throw badArgument(value, index, "table");
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T extends Enum<T>> T getEnum(@Nonnull Object[] args, int index, Class<T> klass) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof String) {
			String name = (String) value;
			try {
				return Enum.valueOf(klass, name.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e) {
				throw new LuaException("Bad name '" + name.toLowerCase(Locale.ENGLISH) + "' for argument " + (index + 1));
			}
		} else {
			throw badArgument(value, index, "string");
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static UUID getUUID(@Nonnull Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof String) {
			String uuid = ((String) value).toLowerCase(Locale.ENGLISH);
			try {
				return UUID.fromString(uuid);
			} catch (IllegalArgumentException e) {
				throw new LuaException("Bad uuid '" + uuid + "' for argument " + (index + 1));
			}
		} else {
			throw badArgument(value, index, "string");
		}
	}

	public static double optNumber(@Nonnull Object[] args, int index, double def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			throw badArgument(value, index, "number");
		}
	}

	public static int optInt(@Nonnull Object[] args, int index, int def) throws LuaException {
		return (int) optNumber(args, index, def);
	}

	public static double optReal(@Nonnull Object[] args, int index, double def) throws LuaException {
		return checkReal(index, optNumber(args, index, def));
	}

	public static boolean optBoolean(@Nonnull Object[] args, int index, boolean def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			throw badArgument(value, index, "boolean");
		}
	}

	public static String optString(@Nonnull Object[] args, int index, String def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof String) {
			return (String) value;
		} else {
			throw badArgument(value, index, "string");
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

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> optTable(@Nonnull Object[] args, int index, Map<Object, Object> def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Map) {
			return (Map<Object, Object>) value;
		} else {
			throw badArgument(value, index, "table");
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

	private static double checkReal(int index, double value) throws LuaException {
		if (Double.isNaN(value)) {
			throw badArgument(index, "number", "nan");
		} else if (value == Double.POSITIVE_INFINITY) {
			throw badArgument(index, "number", "infinity");
		} else if (value == Double.NEGATIVE_INFINITY) {
			throw badArgument(index, "number", "-infinity");
		} else {
			return value;
		}
	}
}
