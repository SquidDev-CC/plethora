package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@Injects
public final class MetaFluidTank extends BasicMetaProvider<IFluidTank> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IFluidTank tank) {
		return Collections.singletonMap("capacity", tank.getCapacity());
	}

	@Nonnull
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
