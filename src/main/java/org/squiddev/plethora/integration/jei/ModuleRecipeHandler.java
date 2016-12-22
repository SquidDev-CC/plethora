package org.squiddev.plethora.integration.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class ModuleRecipeHandler implements IRecipeHandler<ModuleRecipeWrapper> {
	@Nonnull
	@Override
	public Class<ModuleRecipeWrapper> getRecipeClass() {
		return ModuleRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() {
		return ModuleRecipeCategory.ID;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ModuleRecipeWrapper recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ModuleRecipeWrapper recipe) {
		return recipe.isValid();
	}
}
