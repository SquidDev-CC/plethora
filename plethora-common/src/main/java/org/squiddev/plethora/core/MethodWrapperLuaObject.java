package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.TypedLuaObject;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles integration with a {@link ILuaObject}.
 */
public class MethodWrapperLuaObject<T> extends MethodWrapper implements TypedLuaObject<T> {
	public MethodWrapperLuaObject(List<RegisteredMethod<?>> methods, List<UnbakedContext<?>> contexts) {
		super(methods, contexts);
	}

	public MethodWrapperLuaObject(Pair<List<RegisteredMethod<?>>, List<UnbakedContext<?>>> contexts) {
		super(contexts.getLeft(), contexts.getRight());
	}

	@Override
	public Object[] callMethod(@Nonnull ILuaContext luaContext, int method, @Nonnull final Object[] args) throws LuaException, InterruptedException {
		UnbakedContext<?> context = getContext(method);
		@SuppressWarnings("unchecked")
		MethodResult result = getMethod(method).call((IUnbakedContext) context, args);
		return context.getExecutor().execute(result, luaContext);
	}
}
