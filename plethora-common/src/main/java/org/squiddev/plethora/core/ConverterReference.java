package org.squiddev.plethora.core;

import com.google.common.base.Strings;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.converter.ConstantConverter;
import org.squiddev.plethora.api.converter.IConverter;

public class ConverterReference<T> {
	private static final ConstantConverter<Object, Object> identity = x -> x;

	private final int index;
	private final Class<T> tIn;
	private final IConverter<T, ?> converter;

	public ConverterReference(int index, Class<T> tIn, IConverter<T, ?> converter) {
		this.index = index;
		this.tIn = tIn;
		this.converter = converter;
	}

	public static ConverterReference<Object> identity(int index) {
		return new ConverterReference<>(index, Object.class, identity);
	}

	public boolean isIdentity() {
		return converter == identity;
	}

	@SuppressWarnings("unchecked")
	public Object tryConvert(Object[] values) throws LuaException {
		Object value = values[index];
		if (value != null && tIn.isAssignableFrom(value.getClass())) {
			Object converted = converter.convert((T) value);
			if (converted != null) return converted;
		}

		// TODO: Some improved exception method?
		String name = converter.getClass().getSimpleName();
		if (Strings.isNullOrEmpty(name)) name = converter.getClass().getName();
		throw new LuaException("Cannot find object for " + name);
	}
}
