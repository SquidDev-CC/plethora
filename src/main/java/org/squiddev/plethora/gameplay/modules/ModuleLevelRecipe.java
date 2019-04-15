package org.squiddev.plethora.gameplay.modules;

import com.google.gson.JsonObject;
import dan200.computercraft.shared.util.RecipeUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public final class ModuleLevelRecipe extends ShapelessRecipes {
	private ModuleLevelRecipe(String group, ItemStack output, NonNullList<Ingredient> ingredients) {
		super(group, output, ingredients);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack output = getRecipeOutput();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() != output.getItem() || stack.getItemDamage() != output.getItemDamage()) continue;

			ItemStack result = stack.copy();
			NBTTagCompound tag = result.getTagCompound();
			if (tag == null) result.setTagCompound(tag = new NBTTagCompound());
			tag.setInteger("level", tag.hasKey("level", Constants.NBT.TAG_ANY_NUMERIC) ? tag.getInteger("level") + 1 : 1);
			return result;
		}

		return output.copy();
	}

	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			String group = JsonUtils.getString(json, "group", "");
			NonNullList<Ingredient> ingredients = RecipeUtil.getIngredients(context, json);
			ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
			return new ModuleLevelRecipe(group, result, ingredients);
		}
	}
}
