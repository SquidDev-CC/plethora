package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@Injects(AppEng.MOD_ID)
public class MetaCraftingPatternDetails extends BaseMetaProvider<ICraftingPatternDetails> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<ICraftingPatternDetails> context) {
		ICraftingPatternDetails pattern = context.getTarget();
		Map<Object, Object> out = Maps.newHashMap();

		out.put("outputs", getMetaList(context, pattern.getOutputs()));
		out.put("inputs", getMetaList(context, pattern.getInputs()));
		out.put("canSubstitute", pattern.canSubstitute());
		out.put("priority", pattern.getPriority());

		return out;
	}
}
