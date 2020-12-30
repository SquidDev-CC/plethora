package org.squiddev.plethora.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;

public class UseInRecipeCategory implements IRecipeCategory<UseInRecipeWrapper> {
	private static final int RECIPE_WIDTH = 160;
	private static final int RECIPE_HEIGHT = 125;

	private final String id;
	private final IDrawable background;

	public UseInRecipeCategory(String id, IGuiHelper helper) {
		this.id = id;
		background = helper.createBlankDrawable(RECIPE_WIDTH, RECIPE_HEIGHT);
	}

	@Nonnull
	@Override
	public String getUid() {
		return PlethoraCore.ID + ":" + id;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return Helpers.translateToLocal("gui.jei.plethora." + id);
	}

	@Nonnull
	@Override
	public String getModName() {
		return Plethora.NAME;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull UseInRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (RECIPE_WIDTH - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.set(ingredients);
	}
}
