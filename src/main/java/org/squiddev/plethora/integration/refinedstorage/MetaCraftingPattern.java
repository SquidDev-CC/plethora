package org.squiddev.plethora.integration.refinedstorage;

import com.google.common.collect.Maps;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@IMetaProvider.Inject(modId = RS.ID, value = ICraftingPattern.class)
public class MetaCraftingPattern extends BaseMetaProvider<ICraftingPattern> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ICraftingPattern> context) {
		ICraftingPattern pattern = context.getTarget();
		Map<Object, Object> out = Maps.newHashMap();

		out.put("id", pattern.getId());
		out.put("outputs", getMetaList(context, pattern.getOutputs()));
		out.put("inputs", getMetaList(context, pattern.getInputs()));
		out.put("byproducts", getMetaList(context, pattern.getByproducts()));
		out.put("oredict", pattern.isOredict());
		out.put("processing", pattern.isProcessing());
		out.put("valid", pattern.isValid());

		return out;
	}
}
