package org.squiddev.plethora.core.gen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.core.PlethoraCore.LOG;

public final class ArgumentTypeRegistry {
	private ArgumentTypeRegistry() {
	}

	private static final Map<Class<?>, Field> lookup = new HashMap<>();

	@Nullable
	static Field get(Class<?> type) {
		return lookup.get(type);
	}

	public static boolean register(@Nonnull Class<?> klass, @Nonnull Field field) {
		Field existing = lookup.get(klass);
		if (existing != null) {
			LOG.error(
				"ArgumentType field {}.{} and {}.{} both have type {}. Only the first of these will be used.",
				existing.getDeclaringClass().getName(), existing.getName(),
				field.getDeclaringClass().getName(), field.getName(),
				klass.getName()
			);
			return false;
		}

		lookup.put(klass, field);
		return true;
	}
}
