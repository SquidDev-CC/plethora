package org.squiddev.plethora.core.modules;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.api.vehicle.VehicleModuleHandler;

import javax.vecmath.Matrix4f;

/**
 * A version of {@link VehicleModuleHandler} which allows specifying a minecart specific matrix transformation.
 */
public class VehicleModuleHandlerTransform extends VehicleModuleHandler {
	private final Matrix4f transform;

	public VehicleModuleHandlerTransform(ResourceLocation id, Item item, Matrix4f transform) {
		super(id, item);
		this.transform = transform;
	}

	@Override
	protected IVehicleUpgradeHandler createVehicle() {
		return PlethoraAPI.instance().moduleRegistry().toVehicleUpgrade(new ModuleHandlerTransform(this, transform));
	}
}
