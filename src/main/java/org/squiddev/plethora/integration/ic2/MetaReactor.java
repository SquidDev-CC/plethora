package org.squiddev.plethora.integration.ic2;

import ic2.api.reactor.IReactor;
import ic2.core.IC2;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Injects(IC2.MODID)
public final class MetaReactor extends BasicMetaProvider<IReactor> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IReactor object) {
		Map<String, Object> out = new HashMap<>();

		out.put("heat", object.getHeat());
		out.put("heatModifier", object.getHeatEffectModifier());
		out.put("maxHeat", object.getMaxHeat());

		out.put("euOutput", object.getReactorEUEnergyOutput());
		out.put("active", object.produceEnergy());
		out.put("fluidCooled", object.isFluidCooled());

		return Collections.singletonMap("reactor", out);
	}

	@Nonnull
	@Override
	public IReactor getExample() {
		return new TileEntityNuclearReactorElectric();
	}
}
