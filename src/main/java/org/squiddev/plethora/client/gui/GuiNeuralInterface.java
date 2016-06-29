package org.squiddev.plethora.client.gui;

import dan200.computercraft.client.gui.GuiComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import org.squiddev.plethora.neural.NeuralContainer;
import org.squiddev.plethora.neural.NeuralManager;

/**
 * Neural connector GUI
 */
public class GuiNeuralInterface extends GuiComputer {
	public GuiNeuralInterface(final NeuralContainer container) {
		super(
			container,
			ComputerFamily.Advanced,
			NeuralManager.getClient(container.getStack()),
			51,
			19
		);
	}
}
