package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import org.squiddev.plethora.api.WorldLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;

import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.tile;

/**
 * Wraps tile entities and provides them as a peripheral
 * TODO: Blacklisting system
 */
public class PeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te != null) {
			IUnbakedContext<TileEntity> context = new UnbakedContext<TileEntity>(tile(te), new WorldLocation(world, blockPos));
			Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context);

			if (paired.getFirst().size() > 0) {
				// TODO: Get registry name?
				return new PeripheralMethodWrapper(te, paired.getFirst(), paired.getSecond());
			}
		}

		return null;
	}
}
