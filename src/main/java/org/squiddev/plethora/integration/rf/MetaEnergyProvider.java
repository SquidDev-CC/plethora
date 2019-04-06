package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.RedstoneFluxProps;
import cofh.redstoneflux.api.IEnergyHandler;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@IMetaProvider.Inject(value = IEnergyHandler.class, namespace = "rf", modId = RedstoneFluxProps.MOD_ID)
public class MetaEnergyProvider extends BasicMetaProvider<IEnergyHandler> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyHandler object) {
		Map<String, Object> out = new HashMap<>(2);
		out.put("stored", object.getEnergyStored(null));
		out.put("capacity", object.getMaxEnergyStored(null));
		return out;
	}
}
