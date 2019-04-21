package org.squiddev.plethora.integration.ic2;

import ic2.api.energy.prefab.BasicSinkSource;
import ic2.api.energy.tile.*;
import ic2.core.IC2;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider for all EU tiles. Checks for subclasses.
 */
@Injects(IC2.MODID)
public final class MetaEnergyTile extends BasicMetaProvider<IEnergyTile> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IEnergyTile object) {
		Map<String, Object> out = new HashMap<>();

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

		return Collections.singletonMap("eu", out);
	}

	@Nonnull
	@Override
	public IEnergyTile getExample() {
		return new BasicSinkSource(WorldDummy.INSTANCE, BlockPos.ORIGIN, 1000, 3, 2) {
			@Override
			public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, EnumFacing enumFacing) {
				return true;
			}

			@Override
			public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
				return true;
			}
		};
	}
}
