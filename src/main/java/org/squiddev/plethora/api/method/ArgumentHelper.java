package org.squiddev.plethora.api.method;

import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

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
		return new LuaException("Expected " + expected + " +  for argument " + (index + 1) + ", got " + getType(object));
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
	public static Map<Object, Object> defaultTable(@Nonnull Object[] args, int index, Map<Object, Object> def) throws LuaException {
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
		if (value < min || value > max) {
			throw new LuaException(String.format(message, "between " + min + " and " + max));
		}
	}

	public static void assertBetween(int value, int min, int max, String message) throws LuaException {
		if (value < min || value > max) {
			throw new LuaException(String.format(message, "between " + min + " and " + max));
		}
	}
}
