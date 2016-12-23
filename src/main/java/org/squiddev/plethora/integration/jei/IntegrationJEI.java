package org.squiddev.plethora.integration.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;

/**
 * Adds a usage handler which tells you where peripherals/modules can be used
 */
@JEIPlugin
public class IntegrationJEI extends BlankModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();

		registry.addRecipeCategories(new ModuleRecipeCategory(helper));
		registry.addRecipeHandlers(new ModuleRecipeHandler());
		registry.addRecipes(ModuleRecipeWrapper.gatherStacks(registry.getIngredientRegistry().getIngredients(ItemStack.class), helper));

		registry.addRecipeCategoryCraftingItem(new ItemStack(Registry.itemNeuralInterface), ModuleRecipeCategory.ID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(Registry.blockManipulator, 1, 0), ModuleRecipeCategory.ID);
		registry.addRecipeCategoryCraftingItem(new ItemStack(Registry.blockManipulator, 1, 1), ModuleRecipeCategory.ID);
	}
}
