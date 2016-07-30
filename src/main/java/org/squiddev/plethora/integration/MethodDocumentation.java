package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Custom method which provides documentation
 */
public class MethodDocumentation extends BasicMethod {
	private final List<IMethod<?>> methods;

	public MethodDocumentation(List<IMethod<?>> methods) {
		super("getDocs");
		this.methods = methods;
	}

	@Nullable
	@Override
	public String getDocString() {
		return "function([name:string]):string|nil -- Get the documentation for all functions or the function specified. Errors if the function cannot be found.";
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull IUnbakedContext context, @Nonnull Object[] args) throws LuaException {
		String name = ArgumentHelper.optString(args, 0, null);
		if (name == null) {
			Map<String, String> out = Maps.newHashMap();
			for (IMethod method : methods) {
				out.put(method.getName(), method.getDocString());
			}

			return MethodResult.result(out);
		} else {
			// Iterate in reverse
			for (int i = methods.size() - 1; i >= 0; i--) {
				IMethod method = methods.get(i);
				if (method.getName().equals(name)) return MethodResult.result(method.getDocString());
			}

			throw new LuaException("No such method");
		}
	}
}
