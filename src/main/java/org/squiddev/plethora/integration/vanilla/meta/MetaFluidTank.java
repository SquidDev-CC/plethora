package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@IMetaProvider.Inject(IFluidTank.class)
public class MetaFluidTank extends BasicMetaProvider<IFluidTank> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IFluidTank tank) {
		return Collections.singletonMap("capacity", tank.getCapacity());
	}

	@Nullable
	@Override
	public IFluidTank getExample() {
		return new EmptyFluidHandler() {
			@Override
			public int getCapacity() {
				return 1000;
			}
		};
	}
}
