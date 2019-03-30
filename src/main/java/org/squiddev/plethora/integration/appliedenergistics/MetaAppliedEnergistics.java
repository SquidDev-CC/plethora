package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@Injects(AppEng.MOD_ID)
public final class MetaAppliedEnergistics {
	public static final SimpleMetaProvider<ICraftingCPU> META_CRAFTING_CPU = cpu -> {
		Map<Object, Object> out = new HashMap<>(4);
		out.put("name", cpu.getName());
		out.put("busy", cpu.isBusy());
		out.put("coprocessors", cpu.getCoProcessors());
		out.put("storage", cpu.getAvailableStorage());
		return out;
	};

	public static final SimpleMetaProvider<IAEItemStack> META_AE_ITEM_STACK =
		stack -> Collections.singletonMap("count", stack.getStackSize());

	public static final BaseMetaProvider<ICraftingPatternDetails> META_CRAFTING_PATTERN_DETAILS = new BaseMetaProvider<ICraftingPatternDetails>() {
		@Nonnull
		@Override
		public Map<Object, Object> getMeta(@Nonnull IPartialContext<ICraftingPatternDetails> context) {
			ICraftingPatternDetails pattern = context.getTarget();
			Map<Object, Object> out = new HashMap<>(4);

			out.put("outputs", getMetaList(context, pattern.getOutputs()));
			out.put("inputs", getMetaList(context, pattern.getInputs()));
			out.put("canSubstitute", pattern.canSubstitute());
			out.put("priority", pattern.getPriority());

			return out;
		}
	};

	private MetaAppliedEnergistics() {
	}

	@Nonnull
	static HashMap<Object, Object> getItemStackProperties(@Nonnull IAEItemStack stack) {
		HashMap<Object, Object> data = new HashMap<>();
		data.putAll(MetaItemBasic.getBasicMeta(stack.getDefinition()));
		data.put("count", stack.getStackSize());
		data.put("isCraftable", stack.isCraftable());
		return data;
	}
}
