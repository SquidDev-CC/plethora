package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.item.ItemPattern;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.LuaList;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Injects(RS.ID)
public final class MetaItemPattern extends ItemStackContextMetaProvider<ItemPattern> {
	public MetaItemPattern() {
		super("pattern", ItemPattern.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull ItemPattern item) {
		ItemStack stack = context.getTarget();


		IWorldLocation position = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		if (position != null) {
			ICraftingPattern pattern = ItemPattern.getPatternFromCache(position.getWorld(), stack);
			return context.makePartialChild(pattern).getMeta();
		} else {
			Map<String, Object> out = new HashMap<>();

			out.put("id", "normal");
			out.put("outputs", getMetaItems(context, stack, ItemPattern::getOutputSlot));
			out.put("fluidOutputs", getMetaItems(context, stack, ItemPattern::getFluidOutputSlot));
			out.put("inputs", getMetaItems(context, stack, ItemPattern::getInputSlot));
			out.put("fluidInputs", getMetaItems(context, stack, ItemPattern::getFluidInputSlot));
			out.put("oredict", ItemPattern.isOredict(stack));
			out.put("processing", ItemPattern.isProcessing(stack));

			return out;
		}
	}

	private static <T> Map<Integer, TypedMeta<T, ?>> getMetaItems(IPartialContext<?> context, ItemStack stack, IntStackFunction<T> func) {
		LuaList<TypedMeta<T, ?>> out = new LuaList<>(9);
		for (int i = 0; i < 9; i++) {
			T result = func.apply(stack, i);
			if (result != null) out.add(context.makePartialChild(result).getMeta());
		}
		return out.asMap();
	}

	@FunctionalInterface
	public interface IntStackFunction<T> {
		T apply(ItemStack stack, int slot);
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		return getExampleStack();
	}

	@Nonnull
	public static ItemStack getExampleStack() {
		ItemStack stack = new ItemStack(RSItems.PATTERN);
		ItemPattern.setInputSlot(stack, 0, new ItemStack(Blocks.PLANKS));
		ItemPattern.setInputSlot(stack, 1, new ItemStack(Blocks.PLANKS));
		ItemPattern.setOutputSlot(stack, 0, new ItemStack(Items.STICK, 4));
		return stack;
	}
}
