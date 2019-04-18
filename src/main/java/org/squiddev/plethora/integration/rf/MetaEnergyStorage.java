package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyStorage;
import cofh.redstoneflux.impl.EnergyStorage;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(RedstoneFluxProps.MOD_ID)
public final class MetaEnergyStorage extends BasicMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyStorage object) {
		Map<String, Object> out = new HashMap<>(2);
		out.put("stored", object.getEnergyStored());
		out.put("capacity", object.getMaxEnergyStored());
		return Collections.singletonMap("rf", out);
	}

	@Nonnull
	@Override
	public IEnergyStorage getExample() {
		return new EnergyStorage(1000);
	}
}
