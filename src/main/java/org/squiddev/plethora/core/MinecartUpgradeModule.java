package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.minecart.IMinecartAccess;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.executor.ContextDelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.List;

public class MinecartUpgradeModule implements IMinecartUpgradeHandler {
	private final IModuleHandler handler;

	public MinecartUpgradeModule(IModuleHandler handler) {
		this.handler = handler;
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IMinecartAccess access) {
		Pair<IBakedModel, Matrix4f> model = handler.getModel(0);

		Matrix4f transform = new Matrix4f();
		transform.setIdentity();

		// Center the view (-0.5) and then move half a pixel back out.
		transform.setTranslation(new Vector3f(0, 0, -0.5f + (1 / 32.0f)));

		transform.mul(transform, model.getRight());
		return Pair.of(model.getLeft(), transform);
	}

	@Override
	public void update(@Nonnull IMinecartAccess minecart, @Nonnull IPeripheral peripheral) {
		if (peripheral instanceof MethodWrapperPeripheral) {
			IExecutorFactory executor = ((MethodWrapperPeripheral) peripheral).getExecutorFactory();
			if (executor instanceof ITickable) ((ITickable) executor).update();
		}
	}

	@Nullable
	@Override
	public IPeripheral create(@Nonnull IMinecartAccess minecart) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesTurtle.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		MethodRegistry registry = MethodRegistry.instance;
		Entity entity = minecart.getMinecart();

		ICostHandler cost = registry.getCostHandler(entity, null);

		final MinecartModuleAccess access = new MinecartModuleAccess(minecart, handler);
		BasicContextBuilder builder = new BasicContextBuilder();
		handler.getAdditionalContext(access, builder);

		builder.<IWorldLocation>addContext(new EntityWorldLocation(entity));
		builder.addContext(minecart, Reference.id(minecart));

		final IModuleContainer container = access.getContainer();
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				// if (turtle.getUpgrade(side) != TurtleUpgradeModule.this) throw new LuaException("The upgrade is gone");
				// TODO: Correctly invalidate this peripheral when it is detached.
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

	private static final class MinecartModuleAccess implements IModuleAccess {
		private TrackingWrapperPeripheral wrapper;

		private final IMinecartAccess access;
		private final IWorldLocation location;
		private final IModuleContainer container;

		private MinecartModuleAccess(IMinecartAccess access, IModuleHandler handler) {
			this.access = access;
			this.location = new EntityWorldLocation(access.getMinecart());
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
			return access.getData();
		}

		@Override
		public void markDataDirty() {
			access.markDataDirty();
		}

		@Override
		public void queueEvent(@Nonnull String event, @Nullable Object... args) {
			if (wrapper != null) wrapper.queueEvent(event, args);
		}
	}
}
