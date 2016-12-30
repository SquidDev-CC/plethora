package org.squiddev.plethora.integration.ic2;

import com.google.common.collect.Maps;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Provider for all EU tiles. Checks for subclasses.
 */
@IMetaProvider.Inject(value = IEnergyTile.class, namespace = "eu", modId = IC2.MODID)
public class MetaEnergyTile extends BasicMetaProvider<IEnergyTile> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IEnergyTile object) {
		Map<Object, Object> out = Maps.newHashMap();

		if (object instanceof IEnergySink) {
			IEnergySink sink = (IEnergySink) object;
			out.put("sinkTier", sink.getSinkTier());
			out.put("demandedEnergy", sink.getDemandedEnergy());
		}

		if (object instanceof IEnergyConductor) {
			IEnergyConductor conductor = (IEnergyConductor) object;
			out.put("conductionLoss", conductor.getConductionLoss());
			out.put("conductorBreakdownEnergy", conductor.getConductorBreakdownEnergy());
			out.put("insulationBreakdownEnergy", conductor.getInsulationBreakdownEnergy());
			out.put("insulationEnergyAbsorption", conductor.getInsulationEnergyAbsorption());
		}

		if (object instanceof IEnergySource) {
			IEnergySource source = (IEnergySource) object;

			out.put("sourceTier", source.getSourceTier());
			out.put("offeredEnergy", source.getOfferedEnergy());
		}

		return out;
	}
}
