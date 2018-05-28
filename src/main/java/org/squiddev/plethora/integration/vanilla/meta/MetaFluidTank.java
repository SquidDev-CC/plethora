package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.IFluidTank;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(IFluidTank.class)
public class MetaFluidTank extends BasicMetaProvider<IFluidTank> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IFluidTank tank) {
		return Collections.singletonMap("capacity", tank.getCapacity());
	}
}
