package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Displays fluids contained inside a container
 */
@IMetaProvider.Inject(value = IFluidHandler.class, namespace = "tanks")
public class MetaFluidHandler extends BaseMetaProvider<IFluidHandler> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<IFluidHandler> context) {
		IFluidHandler handler = context.getTarget();
		Map<Object, Object> tanks = Maps.newHashMap();
		int i = 0;

		for (IFluidTankProperties tank : handler.getTankProperties()) {
			tanks.put(++i, context.makePartialChild(tank).getMeta());
		}

		return tanks;
	}
}
