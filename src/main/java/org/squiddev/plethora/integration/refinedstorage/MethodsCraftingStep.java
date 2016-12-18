package org.squiddev.plethora.integration.refinedstorage;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingStep;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;

public final class MethodsCraftingStep {
	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingStep.class, worldThread = true,
		doc = "function():table -- Get the pattern for this step."
	)
	public static Object[] getPattern(IContext<ICraftingStep> context, Object[] args) {
		return new Object[]{context.makePartialChild(context.getTarget().getPattern()).getMeta()};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingStep.class, worldThread = true,
		doc = "function():boolean -- Check if this step can start processing."
	)
	public static Object[] canStart(IContext<ICraftingStep> context, Object[] args) {
		return new Object[]{context.getTarget().canStartProcessing()};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingStep.class, worldThread = true,
		doc = "function():table -- Check if this step has started processing."
	)
	public static Object[] isStarted(IContext<ICraftingStep> context, Object[] args) {
		return new Object[]{context.getTarget().hasStartedProcessing()};
	}

	@BasicObjectMethod.Inject(
		modId = RS.ID, value = ICraftingStep.class, worldThread = true,
		doc = "function():boolean -- Check if this step has finished."
	)
	public static Object[] isFinished(IContext<ICraftingStep> context, Object[] args) {
		return new Object[]{context.getTarget().hasReceivedOutputs()};
	}
}
