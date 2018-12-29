package org.squiddev.plethora.gameplay.client.gui;

import dan200.computercraft.client.gui.GuiComputer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.IComputer;
import org.squiddev.plethora.gameplay.minecart.ContainerMinecartComputer;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;

public class GuiMinecartComputer extends GuiComputer {
	public GuiMinecartComputer(EntityMinecartComputer minecart, ClientComputer computer) {
		super(new ContainerMinecartComputer(minecart), minecart.getFamily(), (IComputer) computer, 51, 19);
	}
}
