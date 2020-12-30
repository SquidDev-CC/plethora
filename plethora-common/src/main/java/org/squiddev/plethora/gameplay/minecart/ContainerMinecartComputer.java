package org.squiddev.plethora.gameplay.minecart;

import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerMinecartComputer extends Container implements IContainerComputer {
	private final EntityMinecartComputer minecart;

	public ContainerMinecartComputer(EntityMinecartComputer minecart) {
		this.minecart = minecart;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return minecart.isUsable(player);
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		return minecart.getServerComputer();
	}
}
