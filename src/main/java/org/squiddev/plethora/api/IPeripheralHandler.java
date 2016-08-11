package org.squiddev.plethora.api;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A capability for peripherals which require additional handling. This is intended for objects which require an update
 * such as turtle upgrades or neural interface modules.
 *
 * @see Constants#PERIPHERAL_HANDLER_CAPABILITY
 */
public interface IPeripheralHandler {
	/**
	 * Get the peripheral this handler provides
	 *
	 * @return This handler's peripheral
	 */
	@Nonnull
	IPeripheral getPeripheral();

	/**
	 * Update this peripheral
	 *
	 * @param world    The world this peripheral exists in
	 * @param position The position this peripheral exists at
	 * @param entity   The owning entity
	 */
	void update(@Nonnull World world, @Nonnull Vec3 position, @Nullable EntityLivingBase entity);
}
