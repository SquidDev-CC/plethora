package org.squiddev.plethora.gameplay;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ContainerKeyboard extends Container {
	private final IComputer computer;

	public ContainerKeyboard(IComputer computer) {
		this.computer = computer;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		if (computer instanceof ServerComputer) {
			ServerComputer computer = (ServerComputer) this.computer;

			// Ensure the computer is still loaded
			if (!ComputerCraft.serverComputerRegistry.contains(computer.getInstanceID())) {
				return false;
			}

			World world = computer.getWorld();
			BlockPos pos = computer.getPosition();

			// If we have can find a tile for this computer then check it is usable
			if (world != null && pos != null) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile instanceof TileComputerBase) {
					TileComputerBase tileBase = (TileComputerBase) tile;

					if (tileBase.getServerComputer() == computer && !tileBase.isUsable(player, true)) {
						return false;
					}
				}
			}
		}

		return true;
	}
}
