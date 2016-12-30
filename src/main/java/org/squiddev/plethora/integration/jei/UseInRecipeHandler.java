package org.squiddev.plethora.integration.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;

public class UseInRecipeHandler<T extends UseInRecipeWrapper> implements IRecipeHandler<T> {
	private final String id;
	private final Class<T> klass;

	public UseInRecipeHandler(String id, Class<T> klass) {
		this.id = PlethoraCore.ID + ":" + id;
		this.klass = klass;
	}

	@Nonnull
	@Override
	public Class<T> getRecipeClass() {
		return klass;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public String getRecipeCategoryUid() {
		return id;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid(@Nonnull T recipe) {
		return id;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull T recipe) {
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull T recipe) {
		return recipe.isValid();
	}
}
