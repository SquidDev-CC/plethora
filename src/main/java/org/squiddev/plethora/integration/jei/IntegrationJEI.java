package org.squiddev.plethora.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import javax.annotation.Nonnull;

/**
 * Adds a usage handler which tells you where peripherals/modules can be used
 */
@JEIPlugin
public class IntegrationJEI implements IModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		ModulesWrapper.setup(registry);
		PeripheralsWrapper.setup(registry);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		ModulesWrapper.setup(registry);
		PeripheralsWrapper.setup(registry);
	}
}
