package org.squiddev.plethora.integration.ic2;

import com.google.common.collect.Maps;
import ic2.api.reactor.IReactor;
import ic2.core.IC2;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

@IMetaProvider.Inject(value = IReactor.class, modId = IC2.MODID, namespace = "reactor")
public class MetaReactor extends BasicMetaProvider<IReactor> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IReactor object) {
		Map<Object, Object> out = Maps.newHashMap();

		out.put("heat", object.getHeat());
		out.put("heatModifier", object.getHeatEffectModifier());
		out.put("maxHeat", object.getMaxHeat());

		out.put("euOutput", object.getReactorEUEnergyOutput());
		out.put("active", object.produceEnergy());
		out.put("fluidCooled", object.isFluidCooled());

		return out;
	}
}
