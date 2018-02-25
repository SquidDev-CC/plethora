package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.converter.IConverter;

public class ConverterReference<T> {
	private final int index;
	private final Class<T> tIn;
	private final IConverter<T, ?> converter;

	public ConverterReference(int index, Class<T> tIn, IConverter<T, ?> converter) {
		this.index = index;
		this.tIn = tIn;
		this.converter = converter;
	}

	@SuppressWarnings("unchecked")
	public Object tryConvert(Object[] values) throws LuaException {
		Object value = values[index];
		if (value != null && tIn.isAssignableFrom(value.getClass())) {
			Object converted = converter.convert((T) value);
			if (converted != null) return converted;
		}

		// TODO: Some improved exception method?
		throw new LuaException("Cannot find " + converter.getClass().getSimpleName());
	}
}
