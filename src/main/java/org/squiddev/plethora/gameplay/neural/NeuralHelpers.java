package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.core.MethodRegistry;
import org.squiddev.plethora.core.MethodWrapperPeripheral;
import org.squiddev.plethora.core.UnbakedContext;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.entity;

public final class NeuralHelpers {
	public static final int ARMOR_SLOT = 4;

	private NeuralHelpers() {
		throw new IllegalStateException("Cannot instantiate");
	}

	public static final int INV_SIZE = 6;

	public static ItemStack getStack(EntityLivingBase entity) {
		ItemStack stack = entity.getEquipmentInSlot(ARMOR_SLOT);

		if (stack != null && stack.getItem() == Registry.itemNeuralInterface) {
			return stack;
		} else {
			return null;
		}
	}

	public static IPeripheral buildPeripheral(final IItemHandler handler, final int slot, Entity owner) {
		final ItemStack stack = handler.getStackInSlot(slot);
		if (stack == null || !(stack.getItem() instanceof IModuleItem)) return null;

		// TODO: Switch to proper registry system to allow CC items.
		IModuleItem item = (IModuleItem) stack.getItem();
		final IModule module = item.getModule(stack);

		Collection<IReference<?>> additionalContext = item.getAdditionalContext(stack);

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = entity(owner);
		contextData[contextData.length - 1] = new EntityWorldLocation(owner);

		IUnbakedContext<IModule> context = MethodRegistry.instance.makeContext(new IReference<IModule>() {
			@Nonnull
			@Override
			public IModule get() throws LuaException {
				if (!ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(slot))) {
					throw new LuaException("The module has been removed");
				}
				return module;
			}
		}, CostHelpers.getCostHandler(stack), contextData);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context, UnbakedContext.tryBake(context));
		if (paired.getFirst().size() > 0) {
			return new MethodWrapperPeripheral(module.getModuleId().toString(), stack, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}
}
