package org.squiddev.plethora.impl;

import org.squiddev.plethora.api.PlethoraAPI;
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
}
