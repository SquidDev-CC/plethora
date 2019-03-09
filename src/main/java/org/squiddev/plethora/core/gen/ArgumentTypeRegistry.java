package org.squiddev.plethora.core.gen;

import org.squiddev.plethora.api.method.gen.ArgumentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.core.PlethoraCore.LOG;

public final class ArgumentTypeRegistry {
	private ArgumentTypeRegistry() {
	}

	private static final Map<Class<?>, Field> fields = new HashMap<>();
	private static final Map<Class<?>, ArgumentType<?>> values = new HashMap<>();

	@Nullable
	static Field getField(Class<?> type) {
		return fields.get(type);
	}

	@Nullable
	static ArgumentType<?> get(Class<?> type) {
		return values.get(type);
	}

	public static boolean register(@Nonnull Class<?> klass, @Nonnull Field field) {
		Field existing = fields.get(klass);
		if (existing != null) {
			LOG.error(
				"ArgumentType field {}.{} and {}.{} both have type {}. Only the first of these will be used.",
				existing.getDeclaringClass().getName(), existing.getName(),
				field.getDeclaringClass().getName(), field.getName(),
				klass.getName()
			);
			return false;
		}

		fields.put(klass, field);
		try {
			values.put(klass, (ArgumentType<?>) field.get(null));
		} catch (ReflectiveOperationException e) {
			LOG.error(String.format(
				"ArgumentType field %s.%s's value could not be extracted.",
				field.getDeclaringClass().getName(), field.getName()
			), e);
			return false;
		}
		return true;
	}
}
