package org.squiddev.plethora.neural;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.impl.MethodRegistry;
import org.squiddev.plethora.impl.PeripheralMethodWrapper;
import org.squiddev.plethora.impl.UnbakedContext;
import org.squiddev.plethora.registry.Registry;

import java.util.Collection;
import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.entity;
import static org.squiddev.plethora.api.reference.Reference.id;

public final class NeuralHelpers {
	public static final int ARMOR_SLOT = 3;

	private NeuralHelpers() {
		throw new IllegalStateException("Cannot instantiate");
	}

	public static final int INV_SIZE = 6;

	public static ItemStack getStack(EntityLivingBase entity) {
		ItemStack stack = entity.getCurrentArmor(ARMOR_SLOT);

		if (stack != null && stack.getItem() == Registry.itemNeuralInterface) {
			return stack;
		} else {
			return null;
		}
	}

	public static IPeripheral buildPeripheral(ItemStack stack, Entity owner) {
		if (stack == null || !(stack.getItem() instanceof IModuleItem)) return null;

		IModuleItem item = (IModuleItem) stack.getItem();
		IModule module = item.getModule(stack);

		Collection<IReference<?>> additionalContext = item.getAdditionalContext(stack);

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = entity(owner);
		contextData[contextData.length - 1] = new EntityWorldLocation(owner);

		// TODO: Reference that ensures the module still exists.
		IUnbakedContext<IModule> context = new UnbakedContext<IModule>(id(module), contextData);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context);
		if (paired.getFirst().size() > 0) {
			return new PeripheralMethodWrapper(module.toString(), stack, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}
}
