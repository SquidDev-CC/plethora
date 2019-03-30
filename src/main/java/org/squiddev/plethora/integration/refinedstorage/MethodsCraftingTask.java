package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.wrapper.FromTarget;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;

import java.util.Map;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

public final class MethodsCraftingTask {
	private MethodsCraftingTask() {
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the items which are missing for this task.")
	public static Map<Integer, ?> getMissing(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getMissing().getStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the fludis which are missing for this task.")
	public static Map<Integer, ?> getMissingFluids(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getMissingFluids().getStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the main pattern for this task.")
	public static Map<Object, Object> getPattern(IContext<ICraftingTask> context) {
		return context.makePartialChild(context.getTarget().getPattern()).getMeta();
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Get the various items required for this task.")
	public static Map<Integer, ?> getComponents(IContext<ICraftingTask> context) {
		return getMetaList(context, context.getTarget().getPreviewStacks());
	}

	@PlethoraMethod(modId = RS.ID, doc = "-- Check if this task has finished.")
	public static boolean isFinished(@FromTarget ICraftingTask task) {
		return task.getCompletionPercentage() >= 100;
	}
}
