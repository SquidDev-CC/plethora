package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Custom method which provides documentation
 */
@IMethod.Inject(IMethodCollection.class)
public class MethodDocumentation extends BasicMethod<IMethodCollection> {
	public MethodDocumentation() {
		super("getDocs", "function([name:string]):table|string|nil -- Get the documentation for all functions or the function specified. Errors if the function cannot be found.");
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext<IMethodCollection> context, @Nonnull Object[] args) throws LuaException {
		String name = ArgumentHelper.optString(args, 0, null);
		List<IMethod<?>> methods = context.bake().getTarget().methods();
		if (name == null) {
			Map<String, String> out = Maps.newHashMap();
			for (IMethod method : methods) {
				out.put(method.getName(), method.getDocString());
			}

			return MethodResult.result(out);
		} else {
			for (IMethod method : methods) {
				if (method.getName().equals(name)) return MethodResult.result(method.getDocString());
			}

			throw new LuaException("No such method");
		}
	}
}
