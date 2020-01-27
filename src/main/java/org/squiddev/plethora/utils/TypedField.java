package org.squiddev.plethora.utils;

import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * A type safe, error-swallowing version of {@link Field}.
 *
 * @param <O> The type the field exists on.
 * @param <T> The type of the field itself.
 */
public final class TypedField<O, T> {
	private static final TypedField<?, ?> NONE = new TypedField<>(null);

	private final Field field;

	private TypedField(Field field) {
		this.field = field;
	}

	public void set(@Nonnull O instance, @Nullable T value) {
		if (field == null) return;

		try {
			field.set(instance, value);
		} catch (ReflectiveOperationException | LinkageError e) {
			PlethoraCore.LOG.error("Unable to set {}.{}", field.getDeclaringClass().getName(), field.getName(), e);
		}
	}

	@Nullable
	public T get(@Nonnull O instance) {
		if (field == null) return null;
		try {
			@SuppressWarnings("unchecked")
			T value = (T) field.get(instance);
			return value;
		} catch (ReflectiveOperationException | LinkageError e) {
			PlethoraCore.LOG.error("Unable to get {}.{}", field.getDeclaringClass().getName(), field.getName(), e);
			return null;
		}
	}

	public static <O, T> TypedField<O, T> of(@Nonnull Class<O> type, @Nonnull String deobfField, @Nonnull String obfField) {
		try {
			Field f;
			try {
				f = type.getDeclaredField(obfField);
			} catch (NoSuchFieldException e) {
				f = type.getDeclaredField(deobfField);
			}

			f.setAccessible(true);
			return new TypedField<>(f);
		} catch (NoSuchFieldException | SecurityException e) {
			PlethoraCore.LOG.error("Unable to find {}.{}", type.getName(), deobfField, e);

			@SuppressWarnings({ "unchecked", "rawtypes" })
			TypedField<O, T> empty = (TypedField) NONE;
			return empty;
		}
	}
}
