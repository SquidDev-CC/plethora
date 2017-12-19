package org.squiddev.plethora.api.vehicle;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.module.BasicModuleHandler;

/**
 * A {@link BasicModuleHandler} which also provides a {@link IVehicleUpgradeHandler}.
 */
public class VehicleModuleHandler extends BasicModuleHandler {
	private IVehicleUpgradeHandler handler;

	public VehicleModuleHandler(ResourceLocation id, Item item) {
		super(id, item);
	}

	protected IVehicleUpgradeHandler createVehicle() {
		return PlethoraAPI.instance().moduleRegistry().toVehicleUpgrade(this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
		return super.hasCapability(capability, enumFacing) || capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) {
			IVehicleUpgradeHandler upgrade = handler;
			if (upgrade == null) {
				upgrade = this.handler = createVehicle();
			}
			return (T) upgrade;
		}

		return super.getCapability(capability, enumFacing);
	}
}
