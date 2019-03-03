package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.module.SingletonModuleContainer;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.api.vehicle.IVehicleAccess;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.core.executor.TaskRunner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.List;

public class VehicleUpgradeModule implements IVehicleUpgradeHandler {
	private final IModuleHandler handler;

	public VehicleUpgradeModule(IModuleHandler handler) {
		this.handler = handler;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access) {
		Pair<IBakedModel, Matrix4f> model = handler.getModel(0);

		Matrix4f transform = new Matrix4f();
		transform.setIdentity();

		// Center the view (-0.5) and then move half a pixel back out.
		transform.setTranslation(new Vector3f(0, 0, -0.5f + (1 / 32.0f)));

		transform.mul(transform, model.getRight());
		return Pair.of(model.getLeft(), transform);
	}

	@Override
	public void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral) {
		if (peripheral instanceof MethodWrapperPeripheral) {
			((MethodWrapperPeripheral) peripheral).getRunner().update();
		}
	}

	@Nullable
	@Override
	public IPeripheral create(@Nonnull IVehicleAccess vehicle) {
		final ResourceLocation thisModule = handler.getModule();

		String moduleName = thisModule.toString();
		if (ConfigCore.Blacklist.blacklistModulesVehicle.contains(moduleName) || ConfigCore.Blacklist.blacklistModules.contains(moduleName)) {
			return null;
		}

		MethodRegistry registry = MethodRegistry.instance;
		Entity entity = vehicle.getVehicle();

		ICostHandler cost = CostHelpers.getCostHandler(entity, null);

		final VehicleModuleAccess access = new VehicleModuleAccess(vehicle, handler);

		final IModuleContainer container = access.getContainer();
		IReference<IModuleContainer> containerRef = new ConstantReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() {
				// if (turtle.getUpgrade(side) != TurtleUpgradeModule.this) throw new LuaException("The upgrade is gone");
				// TODO: Correctly invalidate this peripheral when it is detached.
				return container;
			}

			@Nonnull
			@Override
			public IModuleContainer safeGet() {
				return get();
			}
		};

		ContextFactory<IModuleContainer> factory = ContextFactory.of(container, containerRef)
			.withCostHandler(cost)
			.withModules(container, containerRef)
			.addContext(ContextKeys.ORIGIN, new EntityWorldLocation(entity))
			.addContext(ContextKeys.ORIGIN, vehicle, Reference.id(vehicle))
			.addContext(ContextKeys.ORIGIN, vehicle.getVehicle(), Reference.entity(vehicle.getVehicle()));

		handler.getAdditionalContext(access, factory);

		Pair<List<IMethod<?>>, List<UnbakedContext<?>>> paired = registry.getMethodsPaired(factory.getBaked());
		if (paired.getLeft().size() > 0) {
			AttachableWrapperPeripheral peripheral = new AttachableWrapperPeripheral(moduleName, this, paired, new TaskRunner(), factory.getAttachments());
			access.wrapper = peripheral;
			return peripheral;
		} else {
			return null;
		}
	}

	private static final class VehicleModuleAccess implements IModuleAccess {
		private AttachableWrapperPeripheral wrapper;

		private final IVehicleAccess access;
		private final IWorldLocation location;
		private final IModuleContainer container;

		private VehicleModuleAccess(IVehicleAccess access, IModuleHandler handler) {
			this.access = access;
			this.location = new EntityWorldLocation(access.getVehicle());
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
