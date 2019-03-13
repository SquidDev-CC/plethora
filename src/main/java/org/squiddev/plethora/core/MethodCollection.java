package org.squiddev.plethora.core;

import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IMethodCollection;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class MethodCollection implements IMethodCollection {
	private final List<IMethod<?>> methods;

	public MethodCollection(List<IMethod<?>> methods) {
		this.methods = Collections.unmodifiableList(methods);
	}

	@Nonnull
	@Override
	public List<IMethod<?>> methods() {
		return methods;
	}
}
