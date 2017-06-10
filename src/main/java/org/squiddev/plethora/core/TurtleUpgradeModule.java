package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
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
import org.squiddev.plethora.core.executor.ContextDelayedExecutor;
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

	@Nonnull
	@Override
	public ResourceLocation getUpgradeID() {
		return handler.getModule();
	}

	@Override
	public int getLegacyUpgradeID() {
		return -1;
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return adjective;
	}

	@Nonnull
	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingItem() {
		return stack;
	}

	@Override
	public IPeripheral createPeripheral(@Nonnull final ITurtleAccess turtle, @Nonnull final TurtleSide side) {
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
			TrackingWrapperPeripheral peripheral = new TrackingWrapperPeripheral(moduleName, this, paired, new ContextDelayedExecutor(), builder.getAttachments());
			access.wrapper = peripheral;
			return peripheral;
		} else {
			return null;
		}
	}

	@Nonnull
	@Override
	public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction) {
		return null;
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(ITurtleAccess turtle, @Nonnull TurtleSide side) {
		float xOffset = side == TurtleSide.Left ? -0.40625f : 0.40625f;
		Matrix4f transform = new Matrix4f(
			0.0f, 0.0f, 1.0f, -0.5f,
			0.0F, 1.0f, 0.0f, -0.5f,
			-1.0f, 0.0f, 0.0f, 0.5f,
			0.0f, 0.0f, 0.0f, 1.0f
		);


		Pair<IBakedModel, Matrix4f> pair = handler.getModel(0);
		transform.mul(pair.getRight(), transform);

		transform.mul(new Matrix4f(
			0.8f, 0.0f, 0.0f, 0.5f + xOffset,
			0.0f, 0.8f, 0.0f, 0.6f,
			0.0f, 0.0f, 0.8f, 0.475f,
			0.0f, 0.0f, 0.0f, 1.0f
		), transform);

		// Translate -0.5 -0.5 -0.5
		// Rotate Y PI/2
		// Normal transform
		// Scale 0.8
		// Ideally we'd flip if we're on the left side, but that mucks up culling.
		// Translate 0.5 0.5 0.5
		// Translate xOffset 0.1 -0.025

		return Pair.of(pair.getLeft(), transform);
	}

	@Override
	public void update(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side) {
		IPeripheral peripheral = turtle.getPeripheral(side);
		if (peripheral instanceof MethodWrapperPeripheral) {
			IExecutorFactory executor = ((MethodWrapperPeripheral) peripheral).getExecutorFactory();
			if (executor instanceof ITickable) ((ITickable) executor).update();
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
