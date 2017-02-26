package org.squiddev.plethora.integration.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
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

		registry.addRecipeCategories(new UseInRecipeCategory(ID, helper));
		registry.addRecipeHandlers(new UseInRecipeHandler<ModulesWrapper>(ID, ModulesWrapper.class));

		List<ModulesWrapper> wrappers = Lists.newArrayList();
		for (ItemStack stack : registry.getIngredientRegistry().getIngredients(ItemStack.class)) {
			if (isValid(stack)) wrappers.add(new ModulesWrapper(stack, helper));
		}

		registry.addRecipes(wrappers);

		for (ItemStack stack : STACKS) {
			registry.addRecipeCategoryCraftingItem(stack, PlethoraCore.ID + ":" + ID);
		}
	}
}
