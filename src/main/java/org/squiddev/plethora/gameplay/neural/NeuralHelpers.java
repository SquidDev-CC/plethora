package org.squiddev.plethora.gameplay.neural;

import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.BasicModuleContainer;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.MethodWrapperPeripheral;
import org.squiddev.plethora.core.PartialContext;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static org.squiddev.plethora.api.reference.Reference.entity;

public final class NeuralHelpers {
	public static final int ARMOR_SLOT = 4;

	private NeuralHelpers() {
		throw new IllegalStateException("Cannot instantiate");
	}

	public static final int MODULE_SIZE = 5;
	public static final int PERIPHERAL_SIZE = 5;

	public static final int INV_SIZE = MODULE_SIZE + PERIPHERAL_SIZE;

	public static final int BACK = 2;

	public static ItemStack getStack(EntityLivingBase entity) {
		ItemStack stack = entity.getEquipmentInSlot(ARMOR_SLOT);

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

	public static IPeripheral buildModules(final ItemStack[] inventory, Entity owner, IExecutorFactory factory) {
		final ItemStack[] stacks = new ItemStack[MODULE_SIZE];
		Set<ResourceLocation> modules = Sets.newHashSet();

		BasicContextBuilder builder = new BasicContextBuilder();
		for (int i = 0; i < MODULE_SIZE; i++) {
			ItemStack stack = inventory[PERIPHERAL_SIZE + i];
			if (stack == null) continue;

			stack = stacks[i] = stack.copy();

			IModuleHandler moduleHandler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
			if (moduleHandler == null) continue;

			ResourceLocation module = moduleHandler.getModule();
			if (ConfigCore.Blacklist.blacklistModules.contains(module.toString())) continue;

			modules.add(module);
			moduleHandler.getAdditionalContext(builder);
		}

		if (modules.isEmpty()) return null;

		ICostHandler cost = CostHelpers.getCostHandler(owner);
		final IModuleContainer container = new BasicModuleContainer(modules);
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

		builder.<IWorldLocation>addContext(new EntityWorldLocation(owner));
		builder.addContext(owner, entity(owner));

		IUnbakedContext<IModuleContainer> context = MethodRegistry.instance.makeContext(
			containerRef, cost, containerRef, builder.getReferenceArray()
		);

		IPartialContext<IModuleContainer> baked = new PartialContext<IModuleContainer>(
			container, cost, builder.getObjectsArray(), container
		);

		Pair<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, baked);
		if (paired.getLeft().size() > 0) {
			return new MethodWrapperPeripheral("plethora:modules", inventory, paired, factory);
		} else {
			return null;
		}
	}
}
