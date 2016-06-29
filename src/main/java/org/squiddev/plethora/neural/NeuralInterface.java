package org.squiddev.plethora.neural;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import org.squiddev.plethora.api.EntityWorldLocation;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.IModuleItem;
import org.squiddev.plethora.api.reference.IReference;
import org.squiddev.plethora.impl.MethodRegistry;
import org.squiddev.plethora.impl.PeripheralMethodWrapper;
import org.squiddev.plethora.impl.UnbakedContext;
import org.squiddev.plethora.utils.DebugLogger;

import java.util.Collection;
import java.util.List;

import static org.squiddev.plethora.api.reference.Reference.entity;
import static org.squiddev.plethora.api.reference.Reference.id;

public final class NeuralInterface extends ServerComputerManager {


	private final ItemStack[] modules = new ItemStack[6];
	private final boolean[] moduleDirty = new boolean[6];

	public final EntityLivingBase entity;

	public NeuralInterface(EntityLivingBase entity, int instanceId, int sessionId, NBTTagCompound tagCompound) {
		super(instanceId, sessionId);
		this.entity = entity;

		fromNBT(tagCompound);
		turnOn();
	}

	//region INeuralInterface
	public EntityLivingBase getEntity() {
		return entity;
	}

	public ItemStack getUpgrade(EnumFacing direction) {
		Preconditions.checkNotNull(direction, "direction cannot be null");
		if (direction == EnumFacing.UP) throw new IllegalArgumentException("direction cannot be EnumFacing.UP");
		return modules[direction.ordinal()];
	}

	public boolean addUpgrade(EnumFacing direction, ItemStack stack) {
		Preconditions.checkNotNull(direction, "direction cannot be null");
		Preconditions.checkNotNull(stack, "stack cannot be null");

		Item item = stack.getItem();
		if (!(item instanceof IModuleItem)) {
			throw new IllegalArgumentException("upgrade is not an instance of IModuleItem");
		}

		synchronized (modules) {
			int index = direction.ordinal();
			if (modules[index] != null) return false;

			modules[index] = stack;
			moduleDirty[index] = true;
			dirty = true;
		}
		return true;
	}

	public boolean removeUpgrade(EnumFacing direction) {
		Preconditions.checkNotNull(direction, "direction cannot be null");
		if (direction == EnumFacing.UP) throw new IllegalArgumentException("direction cannot be EnumFacing.UP");

		synchronized (modules) {
			int index = direction.ordinal();
			if (modules[index] != null) {
				modules[index] = null;

				dirty = true;
				moduleDirty[index] = true;
				return true;
			}
		}

		return false;
	}
	//endregion

	private IPeripheral buildPeripheral(ItemStack stack) {
		IModuleItem item = (IModuleItem) stack.getItem();
		IModule module = item.getModule(stack);

		Collection<IReference<?>> additionalContext = item.getAdditionalContext(stack);

		IReference<?>[] contextData = new IReference[additionalContext.size() + 2];
		additionalContext.toArray(contextData);
		contextData[contextData.length - 2] = entity(entity);
		contextData[contextData.length - 1] = new EntityWorldLocation(entity);

		// TODO: Reference that ensures the module still exists.
		IUnbakedContext<IModule> context = new UnbakedContext<IModule>(id(module), contextData);

		Tuple<List<IMethod<?>>, List<IUnbakedContext<?>>> paired = MethodRegistry.instance.getMethodsPaired(context);
		if (paired.getFirst().size() > 0) {
			return new PeripheralMethodWrapper(module.toString(), stack, paired.getFirst(), paired.getSecond());
		} else {
			return null;
		}
	}

	//region ServerComputerManager
	@Override
	public void doUpdate(ServerComputer computer) {
		super.doUpdate(computer);
		synchronized (modules) {
			for (int i = 0; i < 6; i++) {
				ItemStack stack = modules[i];
				if (moduleDirty[i]) {
					moduleDirty[i] = false;
					if (stack == null) {
						computer.setPeripheral(i, null);
					} else {
						computer.setPeripheral(i, buildPeripheral(stack));
					}
				}
			}
		}
	}


	@Override
	protected ServerComputer createComputer(int instanceId, String label) {
		ServerComputer computer = super.createComputer(instanceId, label);

		for (int i = 0; i < 6; i++) {
			ItemStack stack = modules[i];
			moduleDirty[i] = false;
			if (stack == null) {
				computer.setPeripheral(i, null);
			} else {
				computer.setPeripheral(i, buildPeripheral(stack));
			}
		}

		return computer;
	}

	@Override
	protected World getWorld() {
		return entity.worldObj;
	}

	@Override
	public void toNBT(NBTTagCompound tag) {
		super.toNBT(tag);
		NBTTagCompound upgradeLookup = new NBTTagCompound();
		for (int i = 0; i < 6; i++) {
			ItemStack stack = modules[i];
			String name = EnumFacing.VALUES[i].toString();
			if (stack == null) {
				upgradeLookup.removeTag(name);
			} else {
				upgradeLookup.setTag(name, stack.serializeNBT());
			}
		}
		tag.setTag("upgrades", upgradeLookup);
	}

	@Override
	public void fromNBT(NBTTagCompound compound) {
		super.fromNBT(compound);

		// Don't read session or instance id.

		NBTTagCompound upgrades = compound.getCompoundTag("upgrades");
		if (upgrades != null) {
			for (Object key : upgrades.getKeySet()) {
				if (key instanceof String) {
					String directionName = (String) key;
					EnumFacing direction = EnumFacing.byName(directionName);
					if (direction == null) {
						DebugLogger.error("Unknown direction %s, ignoring", directionName);
						continue;
					}

					NBTTagCompound tag = upgrades.getCompoundTag(directionName);
					if (tag == null) continue;

					ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
					if (stack != null && stack.getItem() instanceof IModuleItem) {
						addUpgrade(direction, stack);
					}
				}
			}
		}
	}
	//endregion
}
