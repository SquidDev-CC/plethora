package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.vehicle.IVehicleAccess;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public abstract class WirelessModemPeripheralBase extends WirelessModemPeripheral {
	private World world;
	private Vec3d position;

	public WirelessModemPeripheralBase(boolean advanced) {
		super(new ModemState(), advanced);
	}

	@Nonnull
	@Override
	public World getWorld() {
		return world;
	}

	@Nonnull
	@Override
	public Vec3d getPosition() {
		return position;
	}

	public void update(@Nonnull World world, @Nonnull Vec3d position) {
		this.position = position;

		if (this.world != world) {
			this.world = world;
			switchNetwork();
		}
	}

	public static final class PeripheralHandler extends WirelessModemPeripheralBase implements IPeripheralHandler {
		private final ItemStack stack;

		PeripheralHandler(boolean advanced, ItemStack stack) {
			super(advanced);
			this.stack = stack;
		}

		@Override
		public boolean equals(IPeripheral other) {
			return this == other || (other instanceof PeripheralHandler && stack == ((PeripheralHandler) other).stack);
		}

		@Nonnull
		@Override
		public IPeripheral getPeripheral() {
			return this;
		}

		@Override
		public void update(@Nonnull World world, @Nonnull Vec3d position, @Nullable EntityLivingBase entity) {
			update(world, position);
		}
	}

	public static final class VehicleUpgradeHandler extends WirelessModemPeripheralBase implements IVehicleUpgradeHandler {
		@SideOnly(Side.CLIENT)
		private ModelResourceLocation offModel;
		@SideOnly(Side.CLIENT)
		private ModelResourceLocation onModel;

		private final ItemStack stack;

		VehicleUpgradeHandler(boolean advanced, ItemStack stack) {
			super(advanced);
			this.stack = stack;
		}

		@Override
		public boolean equals(IPeripheral other) {
			return this == other || (other instanceof VehicleUpgradeHandler && stack == ((VehicleUpgradeHandler) other).stack);
		}

		@Override
		public void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral) {
			Entity entity = vehicle.getVehicle();
			update(entity.getEntityWorld(), entity.getPositionVector());

			if (peripheral instanceof ModemPeripheral) {
				ModemPeripheral modem = (ModemPeripheral) peripheral;
				if (modem.getModemState().pollChanged()) {
					vehicle.getData().setBoolean("active", modem.getModemState().isOpen());
					vehicle.markDataDirty();
				}
			}
		}

		@Override
		public IPeripheral create(@Nonnull IVehicleAccess vehicle) {
			return this;
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access) {
			loadModelLocations();
			boolean active = access.getData().getBoolean("active");

			ModelManager modelManager = RenderHelpers.getMesher().getModelManager();
			return Pair.of(modelManager.getModel(active ? onModel : offModel), RenderHelpers.getIdentity());
		}

		@SideOnly(Side.CLIENT)
		private void loadModelLocations() {
			if (offModel != null) return;

			if (isInterdimensional()) {
				offModel = new ModelResourceLocation("computercraft:advanced_modem_off", "inventory");
				onModel = new ModelResourceLocation("computercraft:advanced_modem_on", "inventory");
			} else {
				offModel = new ModelResourceLocation("computercraft:wireless_modem_off", "inventory");
				onModel = new ModelResourceLocation("computercraft:wireless_modem_on", "inventory");
			}
		}
	}
}
