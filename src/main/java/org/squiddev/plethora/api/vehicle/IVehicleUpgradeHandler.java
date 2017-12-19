package org.squiddev.plethora.api.vehicle;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

/**
 * A capability which provides an upgrade to various vehicles.
 */
public interface IVehicleUpgradeHandler {
	/**
	 * Get a model from this stack
	 *
	 * @param access The vehicle access
	 * @return A baked model and its transformation
	 * @see net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)
	 */
	@Nonnull
	@SideOnly(Side.CLIENT)
	Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access);

	/**
	 * Update the vehicle handler for the specific
	 */
	void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral);

	/**
	 * Create a peripheral from the given vehicle
	 *
	 * @return The peripheral to create, or {@code null} if none should be created.
	 */
	@Nullable
	IPeripheral create(@Nonnull IVehicleAccess vehicle);
}
