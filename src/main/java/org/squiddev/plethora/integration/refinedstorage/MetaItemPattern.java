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
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@IMetaProvider.Inject(modId = RS.ID, value = ItemStack.class, namespace = "pattern")
public class MetaItemPattern extends BaseMetaProvider<ItemStack> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ItemStack> context) {
		ItemStack stack = context.getTarget();
		if (stack.getItem() != RSItems.PATTERN) return Collections.emptyMap();


		IWorldLocation position = context.getContext(IWorldLocation.class);
		if (position != null) {
			ICraftingPattern pattern = ItemPattern.getPatternFromCache(position.getWorld(), stack);
			return context.makePartialChild(pattern).getMeta();
		} else {
			Map<Object, Object> out = Maps.newHashMap();

			out.put("id", "normal");
			out.put("outputs", getMetaList(context, ItemPattern.getOutputs(stack)));
			out.put("oredict", ItemPattern.isOredict(stack));
			out.put("processing", ItemPattern.isProcessing(stack));

			return out;
		}

	}
}
