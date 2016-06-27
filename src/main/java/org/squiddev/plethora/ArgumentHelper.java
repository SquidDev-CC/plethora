package org.squiddev.plethora;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Map;

/**
 * Various helpers for arguments
 */
public final class ArgumentHelper {
	private ArgumentHelper() {
		throw new IllegalStateException("Cannot create ArgumentHelper");
	}

	public static String getType(Object type) {
		if (type == null) return "nil";
		if (type instanceof String) return "string";
		if (type instanceof Boolean) return "boolean";
		if (type instanceof Number) return "number";
		if (type instanceof Map) return "table";

		DebugLogger.debug("Unknown type of " + type.getClass());
		return "unknown";
	}

	public static LuaException badArgument(Object object, int index, String expected) {
		return new LuaException("Expected " + expected + " +  for argument " + (index + 1) + ", got " + getType(object));
	}

	public static double getNumber(Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			throw badArgument(value, index, "number");
		}
	}

	public static int getInt(Object[] args, int index) throws LuaException {
		return (int) getNumber(args, index);
	}

	public static boolean getBoolean(Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			throw badArgument(value, index, "boolean");
		}
	}

	public static String getString(Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof String) {
			return (String) value;
		} else {
			throw badArgument(value, index, "string");
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getTable(Object[] args, int index) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value instanceof Map) {
			return (Map<Object, Object>) value;
		} else {
			throw badArgument(value, index, "table");
		}
	}

	public static double optNumber(Object[] args, int index, double def) throws LuaException {
		Object value = args.length < index ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			throw badArgument(value, index, "number");
		}
	}

	public static int optInt(Object[] args, int index, int def) throws LuaException {
		return (int) optNumber(args, index, def);
	}

	public static boolean optBoolean(Object[] args, int index, boolean def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			throw badArgument(value, index, "boolean");
		}
	}

	public static String optString(Object[] args, int index, String def) throws LuaException {
		Object value = args.length < index ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof String) {
			return (String) value;
		} else {
			throw badArgument(value, index, "string");
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> defaultTable(Object[] args, int index, Map<Object, Object> def) throws LuaException {
		Object value = index < args.length ? args[index] : null;
		if (value == null) {
			return def;
		} else if (value instanceof Map) {
			return (Map<Object, Object>) value;
		} else {
			throw badArgument(value, index, "table");
		}
	}
}
