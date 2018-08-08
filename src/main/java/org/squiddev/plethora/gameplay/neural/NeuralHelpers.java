package org.squiddev.plethora.gameplay.neural;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import baubles.common.Baubles;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.ConstantReference;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.*;
import org.squiddev.plethora.gameplay.modules.ModulePeripheral;
import org.squiddev.plethora.gameplay.registry.Registry;
import org.squiddev.plethora.utils.LoadedCache;
import org.squiddev.plethora.utils.TinySlot;

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

	@Nullable
	public static TinySlot getSlot(EntityLivingBase entity) {
		ItemStack stack = entity.getItemStackFromSlot(ARMOR_SLOT);

		if (!stack.isEmpty() && stack.getItem() == Registry.itemNeuralInterface) {
			if (entity instanceof EntityPlayer) {
				return new TinySlot.InventorySlot(stack, ((EntityPlayer) entity).inventory);
			} else {
				return new TinySlot(stack);
			}
		}

		if (LoadedCache.hasBaubles() && entity instanceof EntityPlayer) {
			TinySlot slot = getBauble((EntityPlayer) entity);
			if (slot != null) return slot;
		}

		return null;
	}

	@Nonnull
	@Optional.Method(modid = Baubles.MODID)
	public static BaubleType getBaubleType() {
		return BaubleType.HEAD;
	}

	@Nullable
	@Optional.Method(modid = Baubles.MODID)
	public static TinySlot getBauble(@Nonnull EntityPlayer player) {
		IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
		for (int slot : getBaubleType().getValidSlots()) {
			ItemStack stack = handler.getStackInSlot(slot);
			if (stack.getItem() == Registry.itemNeuralInterface) {
				return new TinySlot.BaublesSlot(stack, handler, slot);
			}
		}

		return null;
	}

	@Nonnull
	public static ItemStack getStack(EntityLivingBase entity) {
		TinySlot slot = getSlot(entity);
		return slot == null ? ItemStack.EMPTY : slot.getStack();
	}

	public static IPeripheral buildPeripheral(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) return null;

		IPeripheral peripheral = stack.getCapability(Constants.PERIPHERAL_CAPABILITY, null);
		if (peripheral != null) return peripheral;

		IPeripheralHandler pHandler = stack.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
		if (pHandler != null) return pHandler.getPeripheral();

		return null;
	}

	public static IPeripheral buildModules(final NeuralComputer computer, final NonNullList<ItemStack> inventory, Entity owner) {
		final NonNullList<ItemStack> stacks = NonNullList.withSize(MODULE_SIZE, ItemStack.EMPTY);
		Set<ResourceLocation> modules = Sets.newHashSet();
		Set<IModuleHandler> moduleHandlers = Sets.newHashSet();
		final int moduleHash = computer.getModuleHash();

		for (int i = 0; i < MODULE_SIZE; i++) {
			ItemStack stack = inventory.get(PERIPHERAL_SIZE + i);
			if (stack.isEmpty()) continue;

			stacks.set(i, stack = stack.copy());

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

		ICostHandler cost = CostHelpers.getCostHandler(owner);
		IReference<IModuleContainer> containerRef = new ConstantReference<IModuleContainer>() {
			@Nonnull
			@Override
			public IModuleContainer get() throws LuaException {
				for (int i = 0; i < MODULE_SIZE; i++) {
					ItemStack oldStack = stacks.get(i);
					ItemStack newStack = inventory.get(PERIPHERAL_SIZE + i);
					if (!oldStack.isEmpty() && !ItemStack.areItemStacksEqual(oldStack, newStack)) {
						IModuleHandler moduleHandler = oldStack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
						throw new LuaException("The " + moduleHandler.getModule() + " module has been removed");
					}
				}
				return container;
			}

			@Nonnull
			@Override
			public IModuleContainer safeGet() throws LuaException {
				if (moduleHash != computer.getModuleHash()) {
					throw new LuaException("A module has changed");
				}

				return container;
			}
		};

		ContextFactory<IModuleContainer> builder = ContextFactory.of(container, containerRef)
			.withCostHandler(cost)
			.withModules(container, containerRef)
			.<IWorldLocation>addContext(ContextKeys.ORIGIN, new EntityWorldLocation(owner))
			.addContext(ContextKeys.ORIGIN, owner, entity(owner));

		for (IModuleHandler handler : moduleHandlers) {
			ResourceLocation module = handler.getModule();
			NeuralAccess access = accessMap.get(module);
			if (access == null) {
				accessMap.put(module, access = new NeuralAccess(owner, computer, handler, container));
			}

			handler.getAdditionalContext(access, builder);
		}

		Pair<List<IMethod<?>>, List<UnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(builder.getBaked());
		if (paired.getLeft().size() > 0) {
			ModulePeripheral peripheral = new ModulePeripheral("neuralInterface", owner, paired, computer.getExecutor(), builder.getAttachments(), moduleHash);
			for (NeuralAccess access : accessMap.values()) {
				access.wrapper = peripheral;
			}
			return peripheral;
		} else {
			return null;
		}
	}

	private static final class NeuralAccess implements IModuleAccess {
		private AttachableWrapperPeripheral wrapper;

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
