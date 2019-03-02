package org.squiddev.plethora.integration.jei;


import dan200.computercraft.ComputerCraft;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class UseInRecipeWrapper implements IRecipeWrapper {
	protected static String MINECART_COMPUTER = "entity.plethora.plethora:minecartComputer.name";

	protected final ItemStack stack;
	private final IDrawable slotDrawable;
	private final List<String> usable = new ArrayList<String>();
	private final String id;

	public UseInRecipeWrapper(@Nonnull ItemStack stack, String id, @Nonnull ItemStack[] useIn, @Nonnull IGuiHelper helper) {
		this.stack = stack;
		this.slotDrawable = helper.getSlotDrawable();
		this.id = id;

		for (ItemStack use : useIn) usable.add(use.getDisplayName());

		if (stack.hasCapability(Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY, null)) {
			usable.add(Helpers.translateToLocal(MINECART_COMPUTER));
		}

		if (ComputerCraft.getPocketUpgrade(stack) != null) {
			usable.add(new ItemStack(ComputerCraft.Items.pocketComputer).getDisplayName());
		}

		if (ComputerCraft.getTurtleUpgrade(stack) != null) {
			usable.add(new ItemStack(ComputerCraft.Blocks.turtle).getDisplayName());
		}
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInput(VanillaTypes.ITEM, stack);
	}

	public abstract boolean isValid();

	@Override
	@SideOnly(Side.CLIENT)
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		int xPos = (recipeWidth - slotDrawable.getWidth()) / 2;
		int yPos = 0;
		slotDrawable.draw(minecraft, xPos, yPos);

		xPos = 0;
		yPos += slotDrawable.getHeight() + 4;

		int color = Color.gray.getRGB();
		minecraft.fontRenderer.drawString(
			Translator.translateToLocal("gui.jei.plethora." + id + ".usable"),
			xPos, yPos, color
		);
		yPos += minecraft.fontRenderer.FONT_HEIGHT;

		for (String name : usable) {
			minecraft.fontRenderer.drawString(" - " + name, xPos, yPos, color);
			yPos += minecraft.fontRenderer.FONT_HEIGHT;
		}
	}
}
