package org.squiddev.plethora.integration.ic2;

import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.wiring.TileEntityChargepadBatBox;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for EU storage
 */
@Injects(IC2.MODID)
public final class MetaEnergyStorage extends BasicMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyStorage object) {
		Map<String, Object> out = new HashMap<>(3);
		out.put("stored", object.getStored());
		out.put("capacity", object.getCapacity());
		out.put("output", object.getOutputEnergyUnitsPerTick());
		return Collections.singletonMap("eu_storage", out);
	}

	@Nonnull
	@Override
	public IEnergyStorage getExample() {
		return new TileEntityChargepadBatBox();
	}
}
