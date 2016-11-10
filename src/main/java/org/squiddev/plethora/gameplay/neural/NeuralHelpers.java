package org.squiddev.plethora.gameplay.neural;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.api.reference.Reference;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.MethodWrapperPeripheral;
import org.squiddev.plethora.core.UnbakedContext;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Collections;
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

	public static IPeripheral buildPeripheral(final IItemHandler handler, final int slot) {
		final ItemStack stack = handler.getStackInSlot(slot);
		if (stack == null) return null;

		IPeripheral peripheral = stack.getCapability(Constants.PERIPHERAL_CAPABILITY, null);
		if (peripheral != null) return peripheral;

		IPeripheralHandler pHandler = stack.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
		if (pHandler != null) return pHandler.getPeripheral();

		return null;
	}

	public static IPeripheral buildModules(final IItemHandler handler, Entity owner) {
		final ItemStack[] stacks = new ItemStack[MODULE_SIZE];
		ResourceLocation[] modules = new ResourceLocation[MODULE_SIZE];

		List<IReference<?>> additionalContext = Lists.newArrayList();

		boolean exists = false;
		for (int i = 0; i < MODULE_SIZE; i++) {
			ItemStack stack = stacks[i] = handler.getStackInSlot(PERIPHERAL_SIZE + i);
			if (stack == null) continue;

			IModuleHandler moduleHandler = stack.getCapability(Constants.MODULE_HANDLER_CAPABILITY, null);
			if (moduleHandler == null) continue;

			exists = true;
			modules[i] = moduleHandler.getModule();
			additionalContext.addAll(moduleHandler.getAdditionalContext());
		}

		if (!exists) return null;

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = entity(owner);
		contextData[contextData.length - 1] = new EntityWorldLocation(owner);

		final Set<ResourceLocation> moduleSet = Collections.unmodifiableSet(Sets.newHashSet(modules));

		IModuleContainer container = new IModuleContainer() {
			@Nonnull
			@Override
			public Set<ResourceLocation> get() throws LuaException {
				for (int i = 0; i < MODULE_SIZE; i++) {
					ItemStack newStack = handler.getStackInSlot(PERIPHERAL_SIZE + i);
					if (!ItemStack.areItemStacksEqual(stacks[i], newStack)) {
						throw new LuaException("The module has been removed");
					}
				}
				return moduleSet;
			}
		};

		IUnbakedContext<IModuleContainer> context = MethodRegistry.instance.makeContext(
			Reference.id(container),
			CostHelpers.getCostHandler(owner),
			container,
			contextData
		);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, UnbakedContext.tryBake(context));
		if (paired.getFirst().size() > 0) {
			return new MethodWrapperPeripheral("plethora:modules", handler, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}
}
