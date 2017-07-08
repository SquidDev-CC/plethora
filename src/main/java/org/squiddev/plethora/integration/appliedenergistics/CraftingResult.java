package org.squiddev.plethora.integration.appliedenergistics;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CraftingResult {
	private final Handler handler = new Handler();

	private final IGrid grid;
	private final IComputerAccess access;
	private final IActionHost source;

	private ICraftingJob job = null;
	private ICraftingLink link = null;

	public CraftingResult(@Nonnull IGrid grid, @Nullable IComputerAccess access, @Nonnull IActionHost source) {
		this.grid = grid;
		this.access = access;
		this.source = source;
	}

	public ICraftingCallback getCallback() {
		return handler;
	}

	public ICraftingRequester getRequester() {
		return handler;
	}

	@Nullable
	public ICraftingJob getJob() {
		return job;
	}

	@Nullable
	public ICraftingLink getLink() {
		return link;
	}

	public String getStatus() {
		if (job != null && job.isSimulation()) return "missing";

		if (link == null) {
			return "pending";
		} else if (link.isCanceled()) {
			return "canceled";
		} else if (link.isDone()) {
			return "finished";
		} else {
			return "unknown";
		}
	}

	/**
	 * We have this as a separate class as it implements {@link appeng.api.networking.IGridHost} and we
	 * don't want to expose the methods for that.
	 */
	private final class Handler implements ICraftingCallback, ICraftingRequester {
		@Override
		public void calculationComplete(ICraftingJob job) {
			CraftingResult.this.job = job;

			if (job.isSimulation()) {
				tryQueue(null, "missing");
			} else {
				ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
				link = crafting.submitJob(job, this, null, false, new MachineSource(source));
			}
		}

		@Override
		public ImmutableSet<ICraftingLink> getRequestedJobs() {
			return link == null ? ImmutableSet.of() : ImmutableSet.of(link);
		}

		@Override
		public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack stack, Actionable actionable) {
			return stack;
		}

		@Override
		public void jobStateChange(ICraftingLink link) {
			tryQueue(link.getCraftingID(), getStatus());
		}

		@Override
		public IGridNode getActionableNode() {
			return source.getActionableNode();
		}

		@Override
		public IGridNode getGridNode(AEPartLocation aePartLocation) {
			return source.getGridNode(aePartLocation);
		}

		@Override
		public AECableType getCableConnectionType(AEPartLocation aePartLocation) {
			return source.getCableConnectionType(aePartLocation);
		}

		@Override
		public void securityBreak() {
			source.securityBreak();
		}
	}

	private void tryQueue(Object... args) {
		if (access != null) {
			try {
				access.queueEvent("crafting_status", args);
			} catch (RuntimeException e) {
				DebugLogger.error("Cannot queue crafting callback. Probably detached from the peripheral.", e);
			}
		}
	}

	@BasicObjectMethod.Inject(
		value = CraftingResult.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():boolean -- Check if this crafting task has finished."
	)
	public static Object[] isFinished(IContext<CraftingResult> context, Object[] args) {
		CraftingResult result = context.getTarget();

		ICraftingJob job = result.getJob();
		if (job != null && job.isSimulation()) return new Object[]{true};

		ICraftingLink link = result.getLink();
		return new Object[]{link != null && link.isDone()};
	}

	@BasicObjectMethod.Inject(
		value = CraftingResult.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():boolean -- Check if this crafting task has been canceled."
	)
	public static Object[] isCanceled(IContext<CraftingResult> context, Object[] args) {
		CraftingResult result = context.getTarget();

		ICraftingLink link = result.getLink();
		return new Object[]{link != null && link.isCanceled()};
	}

	@BasicObjectMethod.Inject(
		value = CraftingResult.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():string -- Get the status for this crafting task."
	)
	public static Object[] status(IContext<CraftingResult> context, Object[] args) {
		return new Object[]{context.getTarget().getStatus()};
	}

	@BasicObjectMethod.Inject(
		value = CraftingResult.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():int|nil -- Get the ID for this crafting task."
	)
	public static Object[] getId(IContext<CraftingResult> context, Object[] args) {
		CraftingResult result = context.getTarget();

		ICraftingLink link = result.getLink();
		return new Object[]{link == null ? null : link.getCraftingID()};
	}

	@BasicObjectMethod.Inject(
		value = CraftingResult.class, modId = AppEng.MOD_ID, worldThread = true,
		doc = "function():table -- Get the various items required for this task."
	)
	public static Object[] getComponents(IContext<CraftingResult> context, Object[] args) throws LuaException {
		CraftingResult result = context.getTarget();

		ICraftingJob job = result.getJob();
		if (job == null) throw new LuaException("Task is still pending");

		IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
		job.populatePlan(plan);

		IStorageGrid storage = result.grid.getCache(IStorageGrid.class);
		IMEMonitor<IAEItemStack> monitor = storage.getItemInventory();
		BaseActionSource source = new MachineSource(result.source);

		int i = 0;
		Map<Integer, Map<String, Object>> out = Maps.newHashMapWithExpectedSize(plan.size());
		for (IAEItemStack needed : plan) {
			Map<String, Object> component = Maps.newHashMap();

			IAEItemStack toExtract = needed.copy();
			toExtract.reset();
			toExtract.setStackSize(needed.getStackSize());

			long missing = needed.getStackSize();
			IAEItemStack extracted = monitor.extractItems(toExtract, Actionable.SIMULATE, source);
			if (extracted != null) missing -= extracted.getStackSize();

			component.put("missing", missing);
			component.put("toCraft", needed.getStackSize());
			component.put("component", context.makePartialChild(needed).getMeta());

			out.put(++i, component);
		}

		return new Object[]{out};
	}
}
