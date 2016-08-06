package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Lists all available transfer locations
 */
public class MethodTransferLocations implements IMethod<Object> {
	@Nonnull
	@Override
	public String getName() {
		return "getTransferLocations";
	}

	@Nullable
	@Override
	public String getDocString() {
		return "function():table -- Get a list of all available objects which can be transferred to or from";
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public boolean canApply(@Nonnull IContext<Object> context) {
		return true;
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<Object> context, @Nonnull Object[] args) throws LuaException {
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				Set<String> locations = context.bake().getTransferLocations();
				Map<Integer, String> result = Maps.newHashMap();

				int i = 1;
				for (String location : locations) {
					result.put(i++, location);
				}

				return MethodResult.result(result);
			}
		});
	}
}
