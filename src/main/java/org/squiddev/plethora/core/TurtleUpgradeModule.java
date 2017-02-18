package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.TurtleWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.executor.DelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * Wraps a module item as a turtle upgrade.
 */
class TurtleUpgradeModule implements ITurtleUpgrade {
	private final IModuleHandler handler;
	private final ItemStack stack;
	private final String adjective;

	TurtleUpgradeModule(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		this.handler = handler;
		this.stack = stack;
		this.adjective = adjective;
	}

	@Override
	public ResourceLocation getUpgradeID() {
		return handler.getModule();
	}

	@Override
	public int getLegacyUpgradeID() {
		return -1;
	}

	@Override
	public String getUnlocalisedAdjective() {
		return adjective;
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Override
	public ItemStack getCraftingItem() {
		return stack;
	}

	@Override
	public IPeripheral createPeripheral(final ITurtleAccess turtle, final TurtleSide side) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesTurtle.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		MethodRegistry registry = MethodRegistry.instance;

		TileEntity te = turtle.getWorld().getTileEntity(turtle.getPosition());
		ICostHandler cost;
		if (te != null && te instanceof ITurtleTile) {
			cost = registry.getCostHandler(te, null);
		} else {
			DebugLogger.warn("Cannot find turtle where access says it should be");
			return null;
		}

		final TurtleModuleAccess access = new TurtleModuleAccess(turtle, side, handler);
		BasicContextBuilder builder = new BasicContextBuilder();
		handler.getAdditionalContext(access, builder);

		builder.<IWorldLocation>addContext(new TurtleWorldLocation(turtle));
		builder.addContext(turtle, Reference.id(turtle));

		final IModuleContainer container = access.getContainer();
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				if (turtle.getUpgrade(side) != TurtleUpgradeModule.this) throw new LuaException("The upgrade is gone");
				return container;
			}

			@Nonnull
			@Override
			public IModuleContainer safeGet() throws LuaException {
				return get();
			}
		};

		IUnbakedContext<IModuleContainer> context = registry.makeContext(
			containerRef, cost, containerRef, builder.getReferenceArray());

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container, cost, builder.getObjectsArray(), container
		);

		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = registry.getMethodsPaired(context, baked);
		if (paired.getLeft().size() > 0) {
			TrackingWrapperPeripheral peripheral = new TrackingWrapperPeripheral(moduleName, this, paired, new DelayedExecutor(), builder.getAttachments());
			access.wrapper = peripheral;
			return peripheral;
		} else {
			return null;
		}
	}

	@Override
	public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction) {
		return null;
	}

	@Override
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess turtle, TurtleSide side) {
		float xOffset = side == TurtleSide.Left ? -0.40625f : 0.40625f;
		Matrix4f transform = new Matrix4f(
			0.0F, 0.0F, -0.8F, 0.9f + xOffset,
			0.0F, 0.8F, 0.0F, 0.1F,
			0.8F, 0.0F, 0.0F, 0.075F,
			0.0F, 0.0F, 0.0F, 1.0F
		);

		// Translate -0.5 0 -0.5
		// Rotate -PI/2
		// Scale 0.8
		// Translate 0.5 0 0.5
		// Translate xOffset 0.1 -0.025

		Pair<IBakedModel, Matrix4f> pair = handler.getModel(0);
		transform.mul(transform, pair.getRight());
		return Pair.of(pair.getLeft(), transform);
	}

	@Override
	public void update(ITurtleAccess turtle, TurtleSide side) {
		IPeripheral peripheral = turtle.getPeripheral(side);
		if (peripheral instanceof MethodWrapperPeripheral) {
			IExecutorFactory executor = ((MethodWrapperPeripheral) peripheral).getExecutorFactory();

			if (executor instanceof DelayedExecutor) {
				((DelayedExecutor) executor).update();
			}
		}
	}

	private static final class TurtleModuleAccess implements IModuleAccess {
		private TrackingWrapperPeripheral wrapper;

		private final ITurtleAccess access;
		private final TurtleSide side;
		private final IWorldLocation location;
		private final IModuleContainer container;

		private TurtleModuleAccess(ITurtleAccess access, TurtleSide side, IModuleHandler handler) {
			this.access = access;
			this.side = side;
			this.location = new TurtleWorldLocation(access);
			this.container = new SingletonModuleContainer(handler.getModule());
		}

		@Nonnull
		@Override
		public Object getOwner() {
			return access;
		}

		@Nonnull
		@Override
		public IWorldLocation getLocation() {
			return location;
		}

		@Nonnull
		@Override
		public IModuleContainer getContainer() {
			return container;
		}

		@Nonnull
		@Override
		public NBTTagCompound getData() {
			return access.getUpgradeNBTData(side);
		}

		@Override
		public void markDataDirty() {
			access.updateUpgradeNBTData(side);
		}

		@Override
		public void queueEvent(@Nonnull String event, @Nullable Object... args) {
			if (wrapper != null) wrapper.queueEvent(event, args);
		}
	}
}
