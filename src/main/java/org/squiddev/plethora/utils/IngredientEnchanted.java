package org.squiddev.plethora.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IngredientEnchanted extends Ingredient {
	private final Enchantment enchantment;
	private final int minLevel;

	private final ItemStack[] basicStacks;
	private IntList packed;

	public IngredientEnchanted(Enchantment enchantment, int minLevel) {
		this.enchantment = enchantment;
		this.minLevel = minLevel;

		this.basicStacks = new ItemStack[enchantment.getMaxLevel() - minLevel + 1];

		int i = 0;
		for (int level = minLevel; level <= enchantment.getMaxLevel(); level++) {
			basicStacks[i++] = ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, level));
		}
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks() {
		return basicStacks;
	}

	@Override
	@Nonnull
	public IntList getValidItemStacksPacked() {
		if (packed == null) {
			packed = new IntArrayList();
			for (ItemStack stack : basicStacks) packed.add(RecipeItemHelper.pack(stack));
			packed.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return packed;
	}

	@Override
	public boolean apply(@Nullable ItemStack target) {
		return EnchantmentHelper.getEnchantmentLevel(enchantment, target) >= minLevel;
	}

	@Override
	protected void invalidate() {
		packed = null;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	public static class Factory implements IIngredientFactory {

		@Nonnull
		@Override
		public Ingredient parse(JsonContext context, JsonObject json) {
			ResourceLocation enchantmentId = new ResourceLocation(JsonUtils.getString(json, "id"));
			int level = JsonUtils.getInt(json, "level", 1);

			if (!Enchantment.REGISTRY.containsKey(enchantmentId)) {
				throw new JsonSyntaxException("Unknown enchantment '" + enchantmentId + "'");
			}
			Enchantment enchantment = Enchantment.REGISTRY.getObject(enchantmentId);

			return new IngredientEnchanted(enchantment, level);
		}
	}
}
