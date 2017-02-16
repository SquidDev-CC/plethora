package org.squiddev.plethora.gameplay.minecart;

import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.Optional;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.api.IContainerComputer;

import javax.annotation.Nullable;

@Optional.Interface(modid = CCTweaks.ID, iface = "org.squiddev.cctweaks.api.IContainerComputer")
public class ContainerMinecartComputer extends Container implements IContainerComputer {
	private final EntityMinecartComputer minecart;

	public ContainerMinecartComputer(EntityMinecartComputer minecart) {
		this.minecart = minecart;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return minecart.isUsable(player);
	}

	@Nullable
	@Override
	public IComputer getComputer() {
		return minecart.getServerComputer();
	}
}
