package org.squiddev.plethora.gameplay.neural;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.core.executor.ContextDelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Map;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.*;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.*;

public class NeuralComputer extends ServerComputer {
	private WeakReference<EntityLivingBase> entity;

	private final ItemStack[] stacks = new ItemStack[INV_SIZE];
	private int stackHash;

	private final Map<ResourceLocation, NBTTagCompound> moduleData = Maps.newHashMap();
	private boolean moduleDataDirty = false;

	private final ContextDelayedExecutor executor = new ContextDelayedExecutor();

	public NeuralComputer(World world, int computerID, String label, int instanceID) {
		super(world, computerID, label, instanceID, ComputerFamily.Advanced, WIDTH, HEIGHT);
	}

	public IExecutorFactory getExecutor() {
		return executor;
	}

	public void readModuleData(NBTTagCompound tag) {
		for (String key : tag.getKeySet()) {
			moduleData.put(new ResourceLocation(key), tag.getCompoundTag(key));
		}
	}

	public NBTTagCompound getModuleData(ResourceLocation location) {
		NBTTagCompound tag = moduleData.get(location);
		if (tag == null) moduleData.put(location, tag = new NBTTagCompound());
		return tag;
	}

	public void markModuleDataDirty() {
		moduleDataDirty = true;
	}

	public int getStackHash() {
		return stackHash;
	}

	/**
	 * Update an sync peripherals
	 *
	 * @param owner The owner of the current peripherals
	 */
	public boolean update(@Nonnull EntityLivingBase owner, @Nonnull ItemStack stack, int dirtyStatus) {
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		EntityLivingBase existing = entity == null ? null : entity.get();
		if (!Objects.equal(existing, owner)) {
			dirtyStatus = -1;

			if (!owner.isEntityAlive()) {
				entity = null;
			} else {
				entity = new WeakReference<EntityLivingBase>(existing);
			}
		}

		setWorld(owner.getEntityWorld());
		setPosition(owner.getPosition());

		// Sync changed slots
		if (dirtyStatus != 0) {
			for (int slot = 0; slot < INV_SIZE; slot++) {
				if ((dirtyStatus & (1 << slot)) == 1 << slot) {
					stacks[slot] = handler.getStackInSlot(slot);
				}
			}

			stackHash = Helpers.hashStacks(stacks);
		}

		// Update peripherals
		for (int slot = 0; slot < PERIPHERAL_SIZE; slot++) {
			ItemStack peripheral = stacks[slot];
			if (peripheral == null) continue;

			IPeripheralHandler peripheralHandler = peripheral.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
			if (peripheralHandler != null) {
				peripheralHandler.update(
					owner.worldObj,
					new Vec3d(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ),
					owner
				);
			}
		}

		// Sync modules and peripherals
		if (dirtyStatus != 0) {
			for (int slot = 0; slot < PERIPHERAL_SIZE; slot++) {
				if ((dirtyStatus & (1 << slot)) == 1 << slot) {
					// We skip the "back" slot
					setPeripheral(slot < BACK ? slot : slot + 1, buildPeripheral(stacks[slot]));
				}
			}

			// If the modules have changed.
			if (dirtyStatus >> PERIPHERAL_SIZE != 0) {
				setPeripheral(BACK, NeuralHelpers.buildModules(this, stacks, owner));
			}
		}

		executor.update();

		if (moduleDataDirty) {
			moduleDataDirty = false;

			NBTTagCompound tag = new NBTTagCompound();
			for (Map.Entry<ResourceLocation, NBTTagCompound> entry : moduleData.entrySet()) {
				tag.setTag(entry.getKey().toString(), entry.getValue());
			}
			stack.getTagCompound().setTag(MODULE_DATA, tag);
			return true;
		}

		return false;
	}
}
