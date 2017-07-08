package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.api.IEnergyStorage;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IEnergyStorage.class, namespace = "rf", modId = "CoFHAPI|energy")
public class MetaEnergyStorage extends BasicMetaProvider<IEnergyStorage> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IEnergyStorage object) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("stored", object.getEnergyStored());
		out.put("capacity", object.getMaxEnergyStored());
		return out;
	}
}
