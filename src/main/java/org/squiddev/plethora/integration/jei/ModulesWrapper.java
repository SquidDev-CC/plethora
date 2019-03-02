package org.squiddev.plethora.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.List;

public class ModulesWrapper extends UseInRecipeWrapper {
	private static final String ID = "modules";

	private static final ItemStack[] STACKS = new ItemStack[]{
		new ItemStack(Registry.itemNeuralInterface),
		new ItemStack(Registry.blockManipulator, 1, 0),
		new ItemStack(Registry.blockManipulator, 1, 1),
	};

	private ModulesWrapper(@Nonnull ItemStack stack, @Nonnull IGuiHelper helper) {
		super(stack, ID, STACKS, helper);
	}

	@Override
	public boolean isValid() {
		return isValid(stack);
	}

	private static boolean isValid(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return false;

		return stack.hasCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
	}

	public static void setup(IModRegistry registry) {
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();

		List<ModulesWrapper> wrappers = Lists.newArrayList();
		for (ItemStack stack : registry.getIngredientRegistry().getAllIngredients(VanillaTypes.ITEM)) {
			if (isValid(stack)) wrappers.add(new ModulesWrapper(stack, helper));
		}

		registry.addRecipes(wrappers, PlethoraCore.ID + ":" + ID);

		for (ItemStack stack : STACKS) {
			registry.addRecipeCatalyst(stack, PlethoraCore.ID + ":" + ID);
		}
	}

	public static void setup(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new UseInRecipeCategory(ID, registry.getJeiHelpers().getGuiHelper()));
	}
}
