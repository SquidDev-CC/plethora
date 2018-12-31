package org.squiddev.plethora.integration.refinedstorage;

import com.google.common.collect.Maps;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSItems;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.item.ItemPattern;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@IMetaProvider.Inject(modId = RS.ID, value = ItemStack.class, namespace = "pattern")
public class MetaItemPattern extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();
		if (stack.getItem() != RSItems.PATTERN) return Collections.emptyMap();


		IWorldLocation position = context.getContext(ContextKeys.ORIGIN, IWorldLocation.class);
		if (position != null) {
			ICraftingPattern pattern = ItemPattern.getPatternFromCache(position.getWorld(), stack);
			return context.makePartialChild(pattern).getMeta();
		} else {
			Map<Object, Object> out = Maps.newHashMap();

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

	private static <T> Map<Integer, Map<Object, Object>> getMetaItems(IPartialContext<?> context, ItemStack stack, IntStackFunction<T> func) {
		List<T> out = new ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			T result = func.apply(stack, i);
			if (result != null) out.add(result);
		}
		return getMetaList(context, out);
	}

	public interface IntStackFunction<T> {
		public T apply(ItemStack stack, int slot);
	}
}
