package org.squiddev.plethora.gameplay.neural;

import com.google.common.base.Objects;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.core.executor.DelayedExecutor;
import org.squiddev.plethora.core.executor.IExecutorFactory;

import javax.annotation.Nonnull;
import java.util.UUID;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.HEIGHT;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.WIDTH;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.*;

public class NeuralComputer extends ServerComputer {
	private UUID entityId;

	private final ItemStack[] stacks = new ItemStack[INV_SIZE];

	private final DelayedExecutor executor = new DelayedExecutor();

	public NeuralComputer(World world, int computerID, String label, int instanceID) {
		super(world, computerID, label, instanceID, ComputerFamily.Advanced, WIDTH, HEIGHT);
	}

	public IExecutorFactory getExecutor() {
		return executor;
	}

	/**
	 * Update an sync peripherals
	 *
	 * @param owner The owner of the current peripherals
	 */
	public void update(@Nonnull EntityLivingBase owner, @Nonnull IItemHandler handler, int dirtyStatus) {
		UUID newId = owner.getPersistentID();
		if (!Objects.equal(newId, entityId)) {
			dirtyStatus = -1;

			if (!owner.isEntityAlive()) {
				entityId = null;
			} else {
				entityId = newId;
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
		}

		// Update peripherals
		for (int slot = 0; slot < PERIPHERAL_SIZE; slot++) {
			ItemStack stack = stacks[slot];
			if (stack == null) continue;

			IPeripheralHandler peripheralHandler = stack.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
			if (peripheralHandler != null) {
				peripheralHandler.update(
					owner.worldObj,
					new Vec3(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ),
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
				setPeripheral(BACK, NeuralHelpers.buildModules(stacks, owner, executor));
			}
		}

		executor.update();
	}
}
