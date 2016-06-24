package org.squiddev.plethora.impl;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.registry.Module;

import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.id;
import static org.squiddev.plethora.api.reference.Reference.tile;

/**
 * Core module
 */
public class PeripheralProvider extends Module implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(World world, BlockPos blockPos, EnumFacing enumFacing) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te != null) {
			Context<TileEntity> context = new Context<TileEntity>(null, te, world, blockPos);
			List<IMethod<TileEntity>> methods = MethodRegistry.instance.getMethods(context);

			if (methods.size() > 0) {
				UnbakedContext<TileEntity> unbakedContext = new UnbakedContext<TileEntity>(tile(te), id(world), id(blockPos));
				return new PeripheralMethodWrapper(te, unbakedContext, methods);
			}
		}

		return null;
	}

	@Override
	public void postInit() {
		ComputerCraftAPI.registerPeripheralProvider(this);
	}
}
