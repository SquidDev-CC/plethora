package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyHandler;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(RedstoneFluxProps.MOD_ID)
public final class MetaEnergyProvider extends BasicMetaProvider<IEnergyHandler> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyHandler object) {
		Map<String, Object> out = new HashMap<>(2);
		out.put("stored", object.getEnergyStored(null));
		out.put("capacity", object.getMaxEnergyStored(null));
		return Collections.singletonMap("rf", out);
	}

	@Nonnull
	@Override
	public IEnergyHandler getExample() {
		return new IEnergyHandler() {
			@Override
			public int getEnergyStored(EnumFacing from) {
				return 0;
			}

			@Override
			public int getMaxEnergyStored(EnumFacing from) {
				return 1000;
			}

			@Override
			public boolean canConnectEnergy(EnumFacing from) {
				return false;
			}
		};
	}
}
