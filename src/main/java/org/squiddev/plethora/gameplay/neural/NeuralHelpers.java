package org.squiddev.plethora.gameplay.neural;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.PartialContext;
import org.squiddev.plethora.core.TrackingWrapperPeripheral;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.squiddev.plethora.api.reference.Reference.entity;

public final class NeuralHelpers {
	public static final EntityEquipmentSlot ARMOR_SLOT = EntityEquipmentSlot.HEAD;

	private NeuralHelpers() {
		throw new IllegalStateException("Cannot instantiate");
	}

	public static final int MODULE_SIZE = 5;
	public static final int PERIPHERAL_SIZE = 5;

	public static final int INV_SIZE = MODULE_SIZE + PERIPHERAL_SIZE;

	public static final int BACK = 2;

	public static ItemStack getStack(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(ARMOR_SLOT);

		if (stack != null && stack.getItem() == Registry.itemNeuralInterface) {
			return stack;
		} else {
			return null;
		}
	}

	public static IPeripheral buildPeripheral(@Nullable ItemStack stack) {
		if (stack == null) return null;

		IPeripheral peripheral = stack.getCapability(Constants.PERIPHERAL_CAPABILITY, null);
		if (peripheral != null) return peripheral;

		IPeripheralHandler pHandler = stack.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
		if (pHandler != null) return pHandler.getPeripheral();

		return null;
	}

	public static IPeripheral buildModules(NeuralComputer computer, final ItemStack[] inventory, Entity owner) {
		final ItemStack[] stacks = new ItemStack[MODULE_SIZE];
		Set<ResourceLocation> modules = Sets.newHashSet();
		Set<IModuleHandler> moduleHandlers = Sets.newHashSet();

		for (int i = 0; i < MODULE_SIZE; i++) {
			ItemStack stack = inventory[PERIPHERAL_SIZE + i];
			if (stack == null) continue;

			stack = stacks[i] = stack.copy();

			IModuleHandler moduleHandler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
			if (moduleHandler == null) continue;

			ResourceLocation module = moduleHandler.getModule();
			if (ConfigCore.Blacklist.blacklistModules.contains(module.toString())) continue;

			modules.add(module);
			moduleHandlers.add(moduleHandler);
		}

		if (modules.isEmpty()) return null;

		final IModuleContainer container = new BasicModuleContainer(modules);
		Map<ResourceLocation, NeuralAccess> accessMap = Maps.newHashMap();

		BasicContextBuilder builder = new BasicContextBuilder();
		for (IModuleHandler handler : moduleHandlers) {
			ResourceLocation module = handler.getModule();
			NeuralAccess access = accessMap.get(module);
			if (access == null) {
				accessMap.put(module, access = new NeuralAccess(owner, computer, handler, container));
			}

			handler.getAdditionalContext(access, builder);
		}

		builder.<IWorldLocation>addContext(new EntityWorldLocation(owner));
		builder.addContext(owner, entity(owner));

		ICostHandler cost = CostHelpers.getCostHandler(owner);
		IReference<IModuleContainer> containerRef = new IReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				for (int i = 0; i < MODULE_SIZE; i++) {
					ItemStack oldStack = stacks[i];
					ItemStack newStack = inventory[PERIPHERAL_SIZE + i];
					if (oldStack != null && !ItemStack.areItemStacksEqual(stacks[i], newStack)) {
						IModuleHandler moduleHandler = oldStack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
						throw new LuaException("The " + moduleHandler.getModule() + " module has been removed");
					}
				}
				return container;
			}
		};

		IUnbakedContext<IModuleContainer> context = MethodRegistry.instance.makeContext(
			containerRef, cost, containerRef, builder.getReferenceArray()
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container, cost, builder.getObjectsArray(), container
		);

		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, baked);
		if (paired.getLeft().size() > 0) {
			TrackingWrapperPeripheral peripheral = new TrackingWrapperPeripheral("neuralInterface", owner, paired, computer.getExecutor(), builder.getAttachments());
			for (NeuralAccess access : accessMap.values()) {
				access.wrapper = peripheral;
			}
			return peripheral;
		} else {
			return null;
		}
	}

	private static final class NeuralAccess implements IModuleAccess {
		private TrackingWrapperPeripheral wrapper;

		private final Entity owner;
		private final NeuralComputer computer;
		private final ResourceLocation module;
		private final IModuleContainer container;
		private final IWorldLocation location;

		private NeuralAccess(Entity owner, NeuralComputer computer, IModuleHandler module, IModuleContainer container) {
			this.owner = owner;
			this.computer = computer;
			this.module = module.getModule();
			this.container = container;
			this.location = new EntityWorldLocation(owner);
		}

		@Nonnull
		@Override
		public Object getOwner() {
			return owner;
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
			return computer.getModuleData(module);
		}

		@Override
		public void markDataDirty() {
			computer.markModuleDataDirty();
		}

		@Override
		public void queueEvent(@Nonnull String event, @Nullable Object... args) {
			if (wrapper != null) wrapper.queueEvent(event, args);
		}
	}
}
