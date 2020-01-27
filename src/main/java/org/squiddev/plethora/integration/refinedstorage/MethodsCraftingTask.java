package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.preview.ICraftingPreviewElement;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.squiddev.plethora.api.meta.TypedMeta;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.List;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

public final class MethodsCraftingTask {
	private MethodsCraftingTask() {
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the items which are missing for this task.")
	public static List<TypedMeta<ItemStack, ?>> getMissing(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getMissing().getStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the fludis which are missing for this task.")
	public static List<TypedMeta<FluidStack, ?>> getMissingFluids(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getMissingFluids().getStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the main pattern for this task.")
	public static TypedMeta<ICraftingPattern, ?> getPattern(IContext<ICraftingTask> context) {
		return context.makePartialChild(context.getTarget().getPattern()).getMeta();
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the various items required for this task.")
	public static List<TypedMeta<ICraftingPreviewElement, ?>> getComponents(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getPreviewStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Check if this task has finished.")
	public static boolean isFinished(@FromTarget ICraftingTask task) {
		return task.getCompletionPercentage() >= 100;
	}
}
