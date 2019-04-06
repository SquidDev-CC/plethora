package org.squiddev.plethora.integration.ic2;

import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for EU storage
 */
@IMetaProvider.Inject(value = IEnergyStorage.class, namespace = "eu_storage", modId = IC2.MODID)
public class MetaEnergyStorage extends BasicMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyStorage object) {
		Map<String, Object> out = new HashMap<>(3);
		out.put("stored", object.getStored());
		out.put("capacity", object.getCapacity());
		out.put("output", object.getOutputEnergyUnitsPerTick());
		return out;
	}
}
