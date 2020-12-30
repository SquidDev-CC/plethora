package org.squiddev.plethora.integration;

import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class MetaWrapper<T> implements ConstantReference<MetaWrapper<T>> {
	private final T value;

	public MetaWrapper(@Nonnull T value) {
		Objects.requireNonNull(value, "value cannot be null");
		this.value = value;
	}

	@Nonnull
	public T value() {
		return value;
	}

	public static <T> MetaWrapper<T> of(@Nonnull T value) {
		return new MetaWrapper<>(value);
	}

	@Nonnull
	@Override
	public MetaWrapper<T> get() {
		return this;
	}

	@Nonnull
	@Override
	public MetaWrapper<T> safeGet() {
		return this;
	}

	@Injects
	public static final class MetaProvider extends BaseMetaProvider<MetaWrapper> {
		public MetaProvider() {
			super("Simply wraps an object and exposes metadata for that. You can happily ignore this.");
		}

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<MetaWrapper> context) {
			return context.makePartialChild(context.getTarget().value()).getMeta();
		}
	}
}
