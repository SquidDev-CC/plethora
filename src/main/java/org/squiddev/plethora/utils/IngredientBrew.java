package org.squiddev.plethora.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IngredientBrew extends Ingredient {
	private final Potion potion;
	private final PotionType potionType;

	private final ItemStack[] basicStacks;
	private IntList packed;

	IngredientBrew(Potion potion, PotionType potionType) {
		this.potion = potion;
		this.potionType = potionType;

		this.basicStacks = new ItemStack[3];
		basicStacks[0] = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
		basicStacks[1] = PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potionType);
		basicStacks[2] = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
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
		if (target == null || target.isEmpty()) return false;

		for (PotionEffect effect : PotionUtils.getEffectsFromStack(target)) {
			if (effect.getPotion() == potion) return true;
		}

		return false;
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
			ResourceLocation effect = new ResourceLocation(JsonUtils.getString(json, "effect"));
			if (!Potion.REGISTRY.containsKey(effect)) {
				throw new JsonSyntaxException("Unknown effect '" + effect + "'");
			}

			ResourceLocation potion = new ResourceLocation(JsonUtils.getString(json, "potion"));
			if (!PotionType.REGISTRY.containsKey(potion)) {
				throw new JsonSyntaxException("Unknown potion '" + potion + "'");
			}

			return new IngredientBrew(Potion.REGISTRY.getObject(effect), PotionType.REGISTRY.getObject(potion));
		}
	}
}
