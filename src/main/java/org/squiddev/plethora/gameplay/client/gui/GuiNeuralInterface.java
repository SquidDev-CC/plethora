package org.squiddev.plethora.gameplay.client.gui;

import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.neural.ContainerNeuralInterface;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.Vec2i;

import java.io.IOException;
import java.util.Collections;

import static org.squiddev.plethora.gameplay.neural.ContainerNeuralInterface.*;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.HEIGHT;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.WIDTH;

public class GuiNeuralInterface extends GuiContainer {
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Plethora.RESOURCE_DOMAIN, "textures/gui/neural_interface.png");
	private static final int ICON_Y = 224;

	private final IComputer computer;
	private WidgetTerminal terminalGui;
	private final ContainerNeuralInterface container;
	private boolean peripherals = true;

	public GuiNeuralInterface(ContainerNeuralInterface container) {
		super(container);

		this.container = container;
		computer = ItemComputerHandler.getClient(container.getStack());
		xSize = 254;
		ySize = 217;
	}

	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);
		terminalGui = new WidgetTerminal((width - xSize) / 2 + 8, (height - ySize) / 2 + 8, WIDTH, HEIGHT, () -> computer, 2, 2, 2, 2);
		terminalGui.setAllowFocusLoss(false);
		updateVisible();
	}

	@Override
	public void handleInput() throws IOException {
		// JEI incorrectly sets the repeat events filter, so we force it here.
		boolean previous = Keyboard.areRepeatEventsEnabled();
		if (!previous) Keyboard.enableRepeatEvents(true);

		super.handleInput();

		if (!previous) Keyboard.enableRepeatEvents(false);
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
	protected void keyTyped(char chr, int code) throws IOException {
		if (code == Keyboard.KEY_ESCAPE) {
			super.keyTyped(chr, code);
		} else {
			terminalGui.keyTyped(chr, code);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		terminalGui.mouseClicked(x, y, button);

		int ox = x - guiLeft, oy = y - guiTop;
		if (ox >= SWAP.x && ox < SWAP.x + 16 && oy >= SWAP.y && oy < SWAP.y + 16) {
			peripherals = !peripherals;
			updateVisible();
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int x = Mouse.getEventX() * width / mc.displayWidth;
		int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		terminalGui.handleMouseInput(x, y);
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();
		terminalGui.handleKeyboardInput();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float v, int mouseX, int mouseY) {
		terminalGui.draw(mc, 0, 0, mouseX, mouseY);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(BACKGROUND);

		drawTexturedRect(0, 0, 0, 0, xSize, ySize);
		drawTexturedRect(SWAP.x, SWAP.y, peripherals ? 0 : 16, ICON_Y, 16, 16);

		if (peripherals) {
			drawTexturedRect(NEURAL_START_X + 1 + S, START_Y + 1, 32, ICON_Y, 16, 16); // Top
			drawTexturedRect(NEURAL_START_X + 1, START_Y + 1 + S, 50, ICON_Y, 16 * 3, 16); // Middle
			drawTexturedRect(NEURAL_START_X + 1 + S, START_Y + 1 + 2 * S, 104, ICON_Y, 16, 16); // Bottom
		}
	}

	private void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
		drawTexturedModalRect(guiLeft + x, guiTop + y, u, v, width, height);
	}

	@Override
	public void drawScreen(int x, int y, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(x, y, partialTicks);

		int ox = x - guiLeft, oy = y - guiTop;
		if (ox >= SWAP.x && ox < SWAP.x + 16 && oy >= SWAP.y && oy < SWAP.y + 16) {
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);
			drawGradientRect(guiLeft + SWAP.x, guiTop + SWAP.y, guiLeft + SWAP.x + 16, guiTop + SWAP.y + 16, -2130706433, -2130706433);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();

			drawHoveringText(Collections.singletonList(Helpers.translateToLocal(peripherals ?
				"gui.plethora.neuralInterface.modules" :
				"gui.plethora.neuralInterface.peripherals"
			)), x, y);
		}

		renderHoveredToolTip(x, y);
	}

	private void updateVisible() {
		setVisible(container.peripheralSlots, peripherals);
		setVisible(container.moduleSlots, !peripherals);
	}

	private static void setVisible(Slot[] slots, boolean visible) {
		for (int i = 0, peripheralSlotsLength = slots.length; i < peripheralSlotsLength; i++) {
			Slot slot = slots[i];
			if (visible) {
				Vec2i pos = ContainerNeuralInterface.POSITIONS[i];
				slot.xPos = pos.x;
				slot.yPos = pos.y;
			} else {
				slot.xPos = -2000;
				slot.yPos = -2000;
			}
		}
	}
}
