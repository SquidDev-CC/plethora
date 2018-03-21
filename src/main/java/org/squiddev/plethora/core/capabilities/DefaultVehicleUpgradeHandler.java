package org.squiddev.plethora.core.capabilities;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.vehicle.IVehicleAccess;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import static org.squiddev.plethora.gameplay.client.RenderHelpers.getIdentity;
import static org.squiddev.plethora.gameplay.client.RenderHelpers.getMesher;

public class DefaultVehicleUpgradeHandler implements IVehicleUpgradeHandler {
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access) {
		return Pair.of(getMesher().getModelManager().getMissingModel(), getIdentity());
	}

	@Override
	public void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral) {
	}

	@Nullable
	@Override
	public IPeripheral create(@Nonnull IVehicleAccess vehicle) {
		return null;
	}
}
