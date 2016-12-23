package org.squiddev.plethora.integration.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;

public class ModuleRecipeCategory extends BlankRecipeCategory<ModuleRecipeWrapper> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;

	public static final String ID = PlethoraCore.ID + ":" + "modules";

	private final IDrawable background;

	public ModuleRecipeCategory(IGuiHelper helper) {
		background = helper.createBlankDrawable(recipeWidth, recipeHeight);
	}

	@Nonnull
	@Override
	public String getUid() {
		return ID;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return Helpers.translateToLocal("gui.jei.plethora.modules");
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ModuleRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.setFromRecipe(0, recipeWrapper.getInputs());
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ModuleRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.set(ingredients);
	}
}
