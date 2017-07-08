package org.squiddev.plethora.integration.jei;


import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;

public abstract class UseInRecipeWrapper implements IRecipeWrapper {
	protected final ItemStack stack;
	private final IDrawable slotDrawable;
	private final ItemStack[] usable;
	private final String id;

	public UseInRecipeWrapper(@Nonnull ItemStack stack, String id, @Nonnull ItemStack[] useIn, @Nonnull IGuiHelper helper) {
		this.stack = stack;
		this.slotDrawable = helper.getSlotDrawable();
		this.usable = useIn;
		this.id = id;
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInput(ItemStack.class, stack);
	}

	public abstract boolean isValid();

	@Override
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

		for (ItemStack stack : usable) {
			String name = stack.getDisplayName();
			minecraft.fontRenderer.drawString(" - " + name, xPos, yPos, color);
			yPos += minecraft.fontRenderer.FONT_HEIGHT;
		}
	}
}
