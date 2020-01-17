package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.item.ItemPattern;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@Injects(RS.ID)
public final class MetaCraftingPattern extends BaseMetaProvider<ICraftingPattern> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ICraftingPattern> context) {
		ICraftingPattern pattern = context.getTarget();
		Map<String, Object> out = new HashMap<>();

		out.put("id", pattern.getId());
		out.put("outputs", getMetaList(context, pattern.getOutputs()));
		out.put("fluidInputs", getMetaList(context, pattern.getFluidInputs()));
		out.put("fluidOutputs", getMetaList(context, pattern.getFluidOutputs()));
		out.put("byproducts", getMetaList(context, pattern.getByproducts()));
		out.put("oredict", pattern.isOredict());
		out.put("processing", pattern.isProcessing());
		out.put("valid", pattern.isValid());
		out.put("inputs", Helpers.map(pattern.getInputs(), x -> x.isEmpty() ? null : getMetaList(context, x)));

		return out;
	}

	@Nullable
	@Override
	public ICraftingPattern getExample() {
		return ItemPattern.getPatternFromCache(WorldDummy.INSTANCE, MetaItemPattern.getExampleStack());
	}
}
