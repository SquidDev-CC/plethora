package org.squiddev.plethora.integration;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Lists all available transfer locations
 */
@IMethod.Inject(IMethodCollection.class)
public class MethodTransferLocations implements IMethod<IMethodCollection> {
	@Nonnull
	@Override
	public String getName() {
		return "getTransferLocations";
	}

	@Nullable
	@Override
	public String getDocString() {
		return "function([location:string]):table -- Get a list of all available objects which can be transferred to or from";
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<IMethodCollection> context) {
		return context.getTarget().has(ITransferMethod.class);
	}

	@Nonnull
	@Override
	public MethodResult apply(@Nonnull final IUnbakedContext<IMethodCollection> context, @Nonnull Object[] args) throws LuaException {
		final String location = ArgumentHelper.optString(args, 0, null);
		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IMethodCollection> baked = context.bake();
				Set<String> locations;
				if (location == null) {
					locations = baked.getTransferLocations();
				} else {
					Object found = baked.getTransferLocation(location);
					if (found == null) throw new LuaException("Location '" + location + "' does not exist");

					locations = PlethoraAPI.instance().transferRegistry().getTransferLocations(found, false);
				}
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
