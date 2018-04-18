package org.squiddev.plethora.integration;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.Map;

public class MetaWrapper<T> extends ConstantReference<MetaWrapper<T>> {
	private final T value;

	public MetaWrapper(@Nonnull T value) {
		Preconditions.checkNotNull(value, "value cannot be null");
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
	public MetaWrapper<T> get() throws LuaException {
		return this;
	}

	@Nonnull
	@Override
	public MetaWrapper<T> safeGet() throws LuaException {
		return this;
	}

	@IMetaProvider.Inject(MetaWrapper.class)
	public static class MetaProvider extends BaseMetaProvider<MetaWrapper> {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<MetaWrapper> context) {
			return context.makePartialChild(context.getTarget().value()).getMeta();
		}
	}
}
