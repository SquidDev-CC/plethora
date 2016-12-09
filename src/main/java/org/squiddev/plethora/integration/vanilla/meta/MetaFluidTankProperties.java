package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(IFluidTankProperties.class)
public class MetaFluidTankProperties extends BasicMetaProvider<IFluidTankProperties> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IFluidTankProperties tank) {
		return Collections.<Object, Object>singletonMap("capacity", tank.getCapacity());
	}
}
