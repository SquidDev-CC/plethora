package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.SimpleMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.integration.vanilla.meta.MetaFluidStack;
import org.squiddev.plethora.integration.vanilla.meta.MetaItemBasic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

@Injects(AppEng.MOD_ID)
public final class MetaAppliedEnergistics {
	public static final SimpleMetaProvider<ICraftingCPU> META_CRAFTING_CPU = cpu -> {
		Map<String, Object> out = new HashMap<>(5);
		out.put("name", cpu.getName());
		out.put("busy", cpu.isBusy());
		out.put("coprocessors", cpu.getCoProcessors());
		out.put("storage", cpu.getAvailableStorage());
		if (cpu.isBusy()) out.put("job", CraftingCPU.getCurrentJob(cpu));

		return out;
	};

	public static final IMetaProvider<IAEItemStack> META_AE_ITEM_STACK = new BasicMetaProvider<IAEItemStack>(
		"An ItemStack within an AE network."
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IAEItemStack stack) {
			return Collections.singletonMap("count", stack.getStackSize());
		}

		@Nullable
		@Override
		public IAEItemStack getExample() {
			return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(new ItemStack(Items.STICK, 4));
		}
	};

	public static final BaseMetaProvider<ICraftingPatternDetails> META_CRAFTING_PATTERN_DETAILS = new BaseMetaProvider<ICraftingPatternDetails>() {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<ICraftingPatternDetails> context) {
			ICraftingPatternDetails pattern = context.getTarget();
			Map<String, Object> out = new HashMap<>(4);

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
	static Map<String, ?> getItemStackProperties(@Nonnull IAEItemStack stack) {
		HashMap<String, Object> data = new HashMap<>();
		MetaItemBasic.fillBasicMeta(data, stack.getDefinition());
		data.put("count", stack.getStackSize());
		data.put("isCraftable", stack.isCraftable());
		return data;
	}

	@Nonnull
	static Map<String, ?> getFluidStackProperties(@Nonnull IAEFluidStack stack) {
		return MetaFluidStack.getBasicMeta(stack.getFluidStack());
	}
}
