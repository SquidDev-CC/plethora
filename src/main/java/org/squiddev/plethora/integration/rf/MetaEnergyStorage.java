package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyStorage;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = IEnergyStorage.class, namespace = "rf", modId = RedstoneFluxProps.MOD_ID)
public class MetaEnergyStorage extends BasicMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyStorage object) {
		Map<String, Object> out = new HashMap<>(2);
		out.put("stored", object.getEnergyStored());
		out.put("capacity", object.getMaxEnergyStored());
		return out;
	}
}
