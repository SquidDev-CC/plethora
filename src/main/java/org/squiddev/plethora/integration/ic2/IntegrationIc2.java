package org.squiddev.plethora.integration.ic2;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.converter.DynamicConverter;

@Injects(IC2.MODID)
public final class IntegrationIc2 {
	public static final DynamicConverter<TileEntity, IEnergyTile> TILE_TO_ENERGY_TILE = from -> {
		if (EnergyNet.instance == null) return null;

		World world = from.getWorld();
		BlockPos pos = from.getPos();
		return world == null || pos == null ? null : EnergyNet.instance.getSubTile(world, pos);
	};
}
