package org.squiddev.plethora.core;

import com.google.common.collect.Lists;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IMethodCollection;

import javax.annotation.Nonnull;
import java.util.List;

public class MethodCollection implements IMethodCollection {
	private final List<IMethod<?>> methods;

	public MethodCollection(List<RegisteredMethod<?>> methods) {
		this.methods = Lists.transform(methods, RegisteredMethod::method);
	}

	@Nonnull
	@Override
	public List<IMethod<?>> methods() {
		return methods;
	}
}
