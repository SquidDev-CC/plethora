package org.squiddev.plethora.core;

import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.converter.IConverterRegistry;
import org.squiddev.plethora.api.meta.IMetaRegistry;
import org.squiddev.plethora.api.method.IMethodRegistry;

public final class API implements PlethoraAPI.IPlethoraAPI {
	@Override
	public IMethodRegistry methodRegistry() {
		return MethodRegistry.instance;
	}

	@Override
	public IMetaRegistry metaRegistry() {
		return MetaRegistry.instance;
	}

	@Override
	public IConverterRegistry converterRegistry() {
		return ConverterRegistry.instance;
	}
}
