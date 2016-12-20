package org.squiddev.plethora.integration.jei;


import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ModuleRecipeWrapper extends BlankRecipeWrapper {
	private ItemStack[] moduleStacks = new ItemStack[]{
		new ItemStack(Registry.itemNeuralInterface),
		new ItemStack(Registry.blockManipulator),
	};

	private ItemStack[] peripheralStacks = new ItemStack[]{
		new ItemStack(Registry.itemNeuralInterface),
	};

	@Nonnull
	public final ItemStack stack;

	public boolean peripheral;

	public boolean module;

	@Nonnull
	private final IDrawable slotDrawable;

	public ModuleRecipeWrapper(@Nonnull ItemStack stack, @Nonnull IGuiHelper helper) {
		this.stack = stack;
		this.peripheral = stack.hasCapability(Constants.PERIPHERAL_CAPABILITY, null)
			|| stack.hasCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
		this.module = stack.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null);

		this.slotDrawable = helper.getSlotDrawable();
	}

	@Nonnull
	@Override
	public List getInputs() {
		return Collections.singletonList(stack);
	}

	public boolean isValid() {
		return stack != null && stack.stackSize > 0 && stack.getItem() != null && (peripheral || module);
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		int xPos = (recipeWidth - slotDrawable.getWidth()) / 2;
		int yPos = 0;
		slotDrawable.draw(minecraft, xPos, yPos);

		xPos = 0;
		yPos += slotDrawable.getHeight() + 4;

		int color = Color.gray.getRGB();
		if (module) {
			minecraft.fontRendererObj.drawString(
				Translator.translateToLocal("gui.jei.plethora.modules.module"),
				xPos, yPos, color
			);
			yPos += minecraft.fontRendererObj.FONT_HEIGHT;

			for (ItemStack stack : moduleStacks) {
				String name = Translator.translateToLocal(stack.getItem().getUnlocalizedName() + ".name");

				minecraft.fontRendererObj.drawString(" - " + name, xPos, yPos, color);
				yPos += minecraft.fontRendererObj.FONT_HEIGHT;
			}

			// Add additional spacing between paragraphs.
			yPos += minecraft.fontRendererObj.FONT_HEIGHT;
		}

		if (peripheral) {
			minecraft.fontRendererObj.drawString(
				Translator.translateToLocal("gui.jei.plethora.modules.peripheral"),
				xPos, yPos, color
			);
			yPos += minecraft.fontRendererObj.FONT_HEIGHT;

			for (ItemStack stack : peripheralStacks) {
				String name = Translator.translateToLocal(stack.getItem().getUnlocalizedName() + ".name");

				minecraft.fontRendererObj.drawString(" - " + name, xPos, yPos, color);
				yPos += minecraft.fontRendererObj.FONT_HEIGHT;
			}
		}
	}

	private static boolean isValid(ItemStack stack) {
		if (stack == null || stack.stackSize == 0 || stack.getItem() == null) return false;

		return stack.hasCapability(Constants.PERIPHERAL_CAPABILITY, null)
			|| stack.hasCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null)
			|| stack.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
	}

	public static List<ModuleRecipeWrapper> gatherStacks(List<ItemStack> stacks, IGuiHelper helper) {
		List<ModuleRecipeWrapper> wrappers = Lists.newArrayList();
		for (ItemStack stack : stacks) {
			if (isValid(stack)) wrappers.add(new ModuleRecipeWrapper(stack, helper));
		}

		return wrappers;
	}
}
