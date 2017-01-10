package org.squiddev.plethora.gameplay.client.gui;

import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IComputerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.squiddev.plethora.gameplay.ContainerKeyboard;

import java.io.IOException;

/**
 * A GUI which captures all input
 */
@SideOnly(Side.CLIENT)
public class GuiKeyboard extends GuiScreen {
	private final ContainerKeyboard container;
	private final IComputer computer;
	private WidgetTerminal terminalGui;

	public GuiKeyboard(IComputer computer) {
		this.computer = computer;
		this.container = new ContainerKeyboard(computer);
	}

	@Override
	public void initGui() {
		super.initGui();

		mc.thePlayer.openContainer = container;

		Terminal terminal = computer.getTerminal();
		terminalGui = new WidgetTerminal(0, 0, terminal.getWidth(), terminal.getHeight(), new IComputerContainer() {
			@Override
			public IComputer getComputer() {
				return computer;
			}
		}, 2, 2, 2, 2);
		terminalGui.setAllowFocusLoss(false);
		Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		terminalGui.update();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			super.keyTyped(typedChar, keyCode);
		} else {
			terminalGui.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();
		terminalGui.handleKeyboardInput();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
		drawCenteredString(
			renderer,
			StatCollector.translateToLocal("item.plethora.keyboard.close"),
			width / 2,
			10,
			0xFFFFFF
		);
	}
}
