package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@Injects(RS.ID)
public final class MetaCraftingPattern extends BaseMetaProvider<ICraftingPattern> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ICraftingPattern> context) {
		ICraftingPattern pattern = context.getTarget();
		Map<Object, Object> out = new HashMap<>();

		out.put("id", pattern.getId());
		out.put("outputs", getMetaList(context, pattern.getOutputs()));
		out.put("fluidInputs", getMetaList(context, pattern.getFluidInputs()));
		out.put("fluidOutputs", getMetaList(context, pattern.getFluidOutputs()));
		out.put("byproducts", getMetaList(context, pattern.getByproducts()));
		out.put("oredict", pattern.isOredict());
		out.put("processing", pattern.isProcessing());
		out.put("valid", pattern.isValid());

		{
			int i = 0;
			Map<Integer, Map<Integer, Map<Object, Object>>> inputs = new HashMap<>(0);
			for (NonNullList<ItemStack> stacks : pattern.getInputs()) {
				i++;
				if (stacks.isEmpty()) continue;

				inputs.put(i, getMetaList(context, stacks));
			}
			out.put("inputs", inputs);
		}

		return out;
	}
}
