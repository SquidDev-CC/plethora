package org.squiddev.plethora.gameplay.client.gui;

import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.squiddev.plethora.gameplay.keyboard.ContainerKeyboard;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.glasses.CanvasHandler;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.gameplay.registry.Registration;
import org.squiddev.plethora.utils.Helpers;

import java.io.IOException;

/**
 * A GUI which captures all input
 */
@SideOnly(Side.CLIENT)
public class GuiKeyboard extends GuiScreen {
	private final ContainerKeyboard container;
	private final IComputer computer;
	private WidgetTerminal terminalGui;

	private final MouseHandler glassesMouse;

	public GuiKeyboard(IComputer computer) {
		this.computer = computer;
		this.container = new ContainerKeyboard(computer);

		this.glassesMouse = new MouseHandler("glasses", CanvasHandler.WIDTH, CanvasHandler.HEIGHT);
	}

	@Override
	public void initGui() {
		super.initGui();

		mc.player.openContainer = container;

		terminalGui = new WidgetTerminal(0, 0, 1, 1, () -> computer, 2, 2, 2, 2);
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

	/**
	 * Determines if this keyboard will fire glasses_* events
	 */
	private boolean canInputGlasses() {
		// Extract the active neural interface to ensure it's the one we're currently
		// interacting with.
		ItemStack neural = NeuralHelpers.getStack(mc.player);
		if (neural.isEmpty()) return false;

		IComputer computer = ItemComputerHandler.getClient(neural);
		if (computer != this.computer) return false;

		// Scan the modules looking for glasses.
		IItemHandler handler = neural.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (handler == null) return false;

		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack module = handler.getStackInSlot(i);
			if (module.getItem() == Registration.itemModule && module.getMetadata() == PlethoraModules.GLASSES_ID) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		if (button < 0 || button > 2) return;

		if (canInputGlasses()) {
			glassesMouse.click(mouseX, mouseY, button);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int mouseX = Mouse.getEventX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		int wheelChange = Mouse.getEventDWheel();

		if (canInputGlasses()) {
			glassesMouse.update(mouseX, mouseY, wheelChange);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
		drawCenteredString(
			renderer,
			Helpers.translateToLocal("item.plethora.keyboard.close"),
			width / 2,
			10,
			0xFFFFFF
		);
	}

	private class MouseHandler {
		private final String prefix;
		private int termWidth;
		private int termHeight;

		private int lastButton = -1;
		private int lastX = -1;
		private int lastY = -1;

		MouseHandler(String prefix, int width, int height) {
			this.prefix = prefix;
			this.termWidth = width;
			this.termHeight = height;
		}

		void setSize(int width, int height) {
			this.termWidth = width;
			this.termHeight = height;
		}

		void click(int x, int y, int button) {
			if (button < 0 || button > 2) return;

			int charX = x * termWidth / width;
			int charY = y * termHeight / height;
			charX = Math.min(Math.max(charX, 0), CanvasHandler.WIDTH - 1);
			charY = Math.min(Math.max(charY, 0), CanvasHandler.HEIGHT - 1);

			computer.queueEvent(prefix + "_click", new Object[]{
				button + 1, charX + 1, charY + 1
			});

			lastButton = button;
			lastX = charX;
			lastY = charY;
		}

		public void update(int x, int y, int wheelChange) {
			int charX = x * termWidth / width;
			int charY = y * termHeight / height;
			charX = Math.min(Math.max(charX, 0), CanvasHandler.WIDTH - 1);
			charY = Math.min(Math.max(charY, 0), CanvasHandler.HEIGHT - 1);

			if (lastButton >= 0 && !Mouse.isButtonDown(lastButton)) {
				computer.queueEvent(prefix + "_up", new Object[]{
					lastButton + 1, charX + 1, charY + 1
				});
				lastButton = -1;
			}

			if (wheelChange < 0) {
				computer.queueEvent(prefix + "_scroll", new Object[]{
					1, charX + 1, charY + 1
				});
			} else if (wheelChange > 0) {
				computer.queueEvent(prefix + "_scroll", new Object[]{
					-1, charX + 1, charY + 1
				});
			}

			if (lastButton >= 0 && (charX != lastX || charY != lastY)) {
				computer.queueEvent(prefix + "_drag", new Object[]{
					lastButton + 1, charX + 1, charY + 1
				});
				lastX = charX;
				lastY = charY;
			}
		}
	}
}
