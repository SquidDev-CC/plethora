package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.executor.DelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Wraps a module item as a pocket upgrade.
 */
class PocketUpgradeModule implements IPocketUpgrade {
	private final IModuleHandler handler;
	private final ItemStack stack;
	private final String adjective;

	PocketUpgradeModule(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		this.handler = handler;
		this.stack = stack;
		this.adjective = adjective;
	}

	@Nonnull
	@Override
	public ResourceLocation getUpgradeID() {
		return handler.getModule();
	}

	@Nonnull
	@Override
	public String getUnlocalisedAdjective() {
		return adjective;
	}

	@Override
	public ItemStack getCraftingItem() {
		return stack;
	}

	@Override
	public IPeripheral createPeripheral(@Nonnull final IPocketAccess pocket) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesPocket.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		MethodRegistry registry = MethodRegistry.instance;

		final Entity entity = pocket.getEntity();
		ICostHandler cost;
		if (entity != null) {
			cost = registry.getCostHandler(entity, null);
		} else {
			DebugLogger.warn("Cannot find entity for pocket computer");
			return null;
		}

		final PocketModuleAccess access = new PocketModuleAccess(pocket, handler);
		final IModuleContainer container = access.getContainer();
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				if (!pocket.getUpgrades().containsKey(getUpgradeID())) {
					throw new LuaException("The upgrade is gone");
				}
				return container;
			}
		};

		IWorldLocation location = new IWorldLocation() {
			@Nonnull
			@Override
			public World getWorld() {
				return pocket.getEntity().getEntityWorld();
			}

			@Nonnull
			@Override
			public BlockPos getPos() {
				return pocket.getEntity().getPosition();
			}

			@Nonnull
			@Override
			public Vec3d getLoc() {
				return pocket.getEntity().getPositionVector();
			}

			@Nonnull
			@Override
			public IWorldLocation get() throws LuaException {
				if (pocket.getEntity() == null) {
					throw new LuaException("Entity is not there");
				} else {
					return this;
				}
			}
		};

		BasicContextBuilder builder = new BasicContextBuilder();
		handler.getAdditionalContext(access, builder);
		builder.addContext(location);
		builder.addContext(entity, new IReference<Entity>() {
			@Nonnull
			@Override
			public Entity get() throws LuaException {
				Entity accessEntity = pocket.getEntity();
				if (accessEntity != entity) throw new LuaException("Entity has changed");
				return accessEntity;
			}
		});

		IUnbakedContext<IModuleContainer> context = registry.makeContext(
			containerRef, cost, containerRef, builder.getReferenceArray()
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container, cost, builder.getObjectsArray(), container
		);

		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = registry.getMethodsPaired(context, baked);
		if (paired.getLeft().size() > 0) {
			return new PocketPeripheral(this, access, paired, new DelayedExecutor(), builder.getAttachments());
		} else {
			return null;
		}
	}

	@Override
	public void update(@Nonnull IPocketAccess access, IPeripheral peripheral) {
		if (peripheral instanceof PocketPeripheral) {
			PocketPeripheral methodWrapper = (PocketPeripheral) peripheral;

			// Invalidate peripheral
			if (methodWrapper.getEntity() != access.getEntity()) {
				access.invalidatePeripheral();
			}

			// Update the enqueued method
			IExecutorFactory executor = methodWrapper.getExecutorFactory();
			if (executor instanceof DelayedExecutor) {
				((DelayedExecutor) executor).update();
			}
		}
	}

	@Override
	public boolean onRightClick(@Nonnull World world, @Nonnull IPocketAccess access, IPeripheral peripheral) {
		return false;
	}

	private static final class PocketPeripheral extends TrackingWrapperPeripheral {
		private final Entity entity;

		public PocketPeripheral(PocketUpgradeModule owner, PocketModuleAccess access, Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> methods, IExecutorFactory factory, List<IAttachable> attachments) {
			super(owner.getUpgradeID().toString(), owner, methods, factory, attachments);
			this.entity = access.entity;
			access.wrapper = this;
		}

		public Entity getEntity() {
			return entity;
		}
	}

	private static final class PocketModuleAccess implements IModuleAccess {
		private TrackingWrapperPeripheral wrapper;

		private final IPocketAccess access;
		private final Entity entity;
		private final IWorldLocation location;
		private final IModuleContainer container;

		private PocketModuleAccess(IPocketAccess access, IModuleHandler handler) {
			this.entity = access.getEntity();
			this.location = new EntityWorldLocation(entity);
			this.access = access;
			this.container = new SingletonModuleContainer(handler.getModule());
		}

		@Nonnull
		@Override
		public Object getOwner() {
			return entity;
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
			return access.getUpgradeNBTData();
		}

		@Override
		public void markDataDirty() {
			access.updateUpgradeNBTData();
		}

		@Override
		public void queueEvent(@Nonnull String event, @Nullable Object... args) {
			if (wrapper != null) wrapper.queueEvent(event, args);
		}
	}
}
