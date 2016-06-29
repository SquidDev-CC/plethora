package org.squiddev.plethora.client.gui;

import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IComputerContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.squiddev.plethora.Plethora;
import org.squiddev.plethora.neural.ContainerNeuralInterface;
import org.squiddev.plethora.neural.ItemComputerHandler;

import java.io.IOException;

import static org.squiddev.plethora.neural.ItemComputerHandler.HEIGHT;
import static org.squiddev.plethora.neural.ItemComputerHandler.WIDTH;

public class GuiNeuralInterface extends GuiContainer {
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Plethora.RESOURCE_DOMAIN, "textures/gui/neuralInterface.png");

	private final IComputer computer;
	private WidgetTerminal terminalGui;

	public GuiNeuralInterface(InventoryPlayer player, ItemStack stack) {
		super(new ContainerNeuralInterface(player, stack));

		computer = ItemComputerHandler.getClient(stack);
		xSize = 254;
		ySize = 217;
	}

	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);
		terminalGui = new WidgetTerminal((width - xSize) / 2 + 8, (height - ySize) / 2 + 8, WIDTH, HEIGHT, new IComputerContainer() {
			@Override
			public IComputer getComputer() {
				return computer;
			}
		}, 2, 2, 2, 2);
		terminalGui.setAllowFocusLoss(false);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
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
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int x = Mouse.getEventX() * width / mc.displayHeight;
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
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
}
