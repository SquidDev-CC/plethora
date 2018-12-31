package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

import static org.squiddev.plethora.api.method.ContextHelpers.getMetaList;

public final class MethodsCraftingTask {
	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingTask.class,
		doc = "function():table -- Get the items which are missing for this task."
	)
	public static Object[] getMissing(IContext<ICraftingTask> context, Object[] args) {
		return new Object[]{getMetaList(context, context.getTarget().getMissing().getStacks())};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingTask.class,
		doc = "function():table -- Get the fludis which are missing for this task."
	)
	public static Object[] getMissingFluids(IContext<ICraftingTask> context, Object[] args) {
		return new Object[]{getMetaList(context, context.getTarget().getMissingFluids().getStacks())};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingTask.class,
		doc = "function():table -- Get the main pattern for this task."
	)
	public static Object[] getPattern(IContext<ICraftingTask> context, Object[] args) {
		return new Object[]{context.makePartialChild(context.getTarget().getPattern()).getMeta()};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingTask.class,
		doc = "function():table -- Get the various items required for this task."
	)
	public static Object[] getComponents(IContext<ICraftingTask> context, Object[] args) {
		return new Object[]{getMetaList(context, context.getTarget().getPreviewStacks())};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingTask.class,
		doc = "function():boolean -- Check if this task has finished."
	)
	public static Object[] isFinished(IContext<ICraftingTask> context, Object[] args) {
		return new Object[]{context.getTarget().getCompletionPercentage() >= 100};
	}
}
