package org.squiddev.plethora.integration.rf;

import cofh.redstoneflux.api.IEnergyProvider;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IEnergyProvider.class, namespace = "rf", modId = "CoFHAPI|energy")
public class MetaEnergyProvider extends BasicMetaProvider<IEnergyProvider> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IEnergyProvider object) {
		Map<Object, Object> out = Maps.newHashMap();
		out.put("stored", object.getEnergyStored(null));
		out.put("capacity", object.getMaxEnergyStored(null));
		return out;
	}
}
