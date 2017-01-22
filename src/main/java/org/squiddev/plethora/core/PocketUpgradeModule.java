package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.squiddev.cctweaks.api.pocket.IPocketAccess;
import org.squiddev.cctweaks.api.pocket.IPocketUpgrade;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.executor.DelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.utils.DebugLogger;

import javax.annotation.Nonnull;
import java.util.Collection;
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
	public IPeripheral createPeripheral(@Nonnull final IPocketAccess access) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesPocket.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		final IModuleContainer container = new SingletonModuleContainer(thisModule);
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				if (!access.getUpgrades().containsKey(getUpgradeID())) {
					throw new LuaException("The upgrade is gone");
				}
				return container;
			}
		};

		MethodRegistry registry = MethodRegistry.instance;

		final Entity entity = access.getEntity();
		ICostHandler cost;
		if (entity != null) {
			cost = registry.getCostHandler(entity, null);
		} else {
			DebugLogger.warn("Cannot find entity for pocket computer");
			return null;
		}

		IWorldLocation location = new IWorldLocation() {
			@Nonnull
			@Override
			public World getWorld() {
				return access.getEntity().getEntityWorld();
			}

			@Nonnull
			@Override
			public BlockPos getPos() {
				return access.getEntity().getPosition();
			}

			@Nonnull
			@Override
			public Vec3 getLoc() {
				return access.getEntity().getPositionVector();
			}

			@Nonnull
			@Override
			public IWorldLocation get() throws LuaException {
				if (access.getEntity() == null) {
					throw new LuaException("Entity is not there");
				} else {
					return this;
				}
			}
		};

		Collection<IReference<?>> additionalContext = handler.getAdditionalContext();
		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = location;
		contextData[contextData.length - 1] = new IReference<Entity>() {
			@Nonnull
			@Override
			public Entity get() throws LuaException {
				Entity accessEntity = access.getEntity();
				if (accessEntity != entity) throw new LuaException("Entity has changed");
				return accessEntity;
			}
		};

		IUnbakedContext<IModuleContainer> context = registry.makeContext(
			containerRef,
			cost,
			containerRef,
			contextData
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container,
			cost,
			new Object[]{location, entity},
			container
		);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = registry.getMethodsPaired(context, baked);
		if (paired.getFirst().size() > 0) {
			return new PocketPeripheral(this, access, paired.getFirst(), paired.getSecond(), new DelayedExecutor());
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

	private static final class PocketPeripheral extends MethodWrapperPeripheral {
		private final Entity entity;

		public PocketPeripheral(PocketUpgradeModule owner, IPocketAccess access, List<IMethod<?>> methods, List<IUnbakedContext<?>> contexts, IExecutorFactory factory) {
			super(owner.getUpgradeID().toString(), owner, methods, contexts, factory);
			this.entity = access.getEntity();
		}

		public Entity getEntity() {
			return entity;
		}
	}
}
