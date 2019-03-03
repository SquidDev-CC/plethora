package org.squiddev.plethora.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class IngredientEnchanted extends Ingredient {
	private final Enchantment enchantment;
	private final int minLevel;

	private ItemStack[] stacks;
	private IntList packed;

	IngredientEnchanted(Enchantment enchantment, int minLevel) {
		this.enchantment = enchantment;
		this.minLevel = minLevel;
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks() {
		if (stacks != null) return stacks;
		if (enchantment.type == null) return stacks = new ItemStack[0];

		// Find any item which matches this predicate
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for (Item item : Item.REGISTRY) {
			if (enchantment.type != null && enchantment.type.canEnchantItem(item)) {
				for (int level = minLevel; level <= enchantment.getMaxLevel(); level++) {
					ItemStack stack = new ItemStack(item);
					EnchantmentHelper.setEnchantments(Collections.singletonMap(enchantment, level), stack);
					stacks.add(stack);
				}
			}
		}
		return this.stacks = stacks.toArray(new ItemStack[0]);
	}

	@Override
	@Nonnull
	public IntList getValidItemStacksPacked() {
		if (packed != null) return packed;

		packed = new IntArrayList();
		for (ItemStack stack : getMatchingStacks()) packed.add(RecipeItemHelper.pack(stack));
		packed.sort(IntComparators.NATURAL_COMPARATOR);

		return packed;
	}

	@Override
	public boolean apply(@Nullable ItemStack target) {
		if (target == null || target.isEmpty()) return false;

		NBTTagList enchantments = target.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(target) : target.getEnchantmentTagList();
		for (int i = 0; i < enchantments.tagCount(); ++i) {
			NBTTagCompound tag = enchantments.getCompoundTagAt(i);
			Enchantment itemEnchant = Enchantment.getEnchantmentByID(tag.getShort("id"));
			if (itemEnchant == enchantment) return (int) tag.getShort("lvl") >= minLevel;
		}

		return false;
	}

	@Override
	protected void invalidate() {
		stacks = null;
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
