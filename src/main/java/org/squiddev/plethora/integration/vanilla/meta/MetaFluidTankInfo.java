package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.FluidTankInfo;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(value = FluidTankInfo.class, namespace = "fluidTank")
public class MetaFluidTankInfo extends BasicMetaProvider<FluidTankInfo> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull FluidTankInfo object) {
		return Collections.<Object, Object>singletonMap("capacity", object.capacity);
	}
}
