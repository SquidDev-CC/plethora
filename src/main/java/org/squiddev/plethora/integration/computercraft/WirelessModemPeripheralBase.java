package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.WirelessModemPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.minecart.IMinecartAccess;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public abstract class WirelessModemPeripheralBase extends WirelessModemPeripheral {
	private World world;
	private Vec3d position;

	public WirelessModemPeripheralBase(boolean advanced) {
		super(advanced);
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

		public PeripheralHandler(boolean advanced, ItemStack stack) {
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

	public static final class MinecartUpgradeHandler extends WirelessModemPeripheralBase implements IMinecartUpgradeHandler {
		@SideOnly(Side.CLIENT)
		private ModelResourceLocation offModel;
		@SideOnly(Side.CLIENT)
		private ModelResourceLocation onModel;

		private final ItemStack stack;

		public MinecartUpgradeHandler(boolean advanced, ItemStack stack) {
			super(advanced);
			this.stack = stack;
		}

		@Override
		public boolean equals(IPeripheral other) {
			return this == other || (other instanceof MinecartUpgradeHandler && stack == ((MinecartUpgradeHandler) other).stack);
		}

		@Override
		public void update(@Nonnull IMinecartAccess access, @Nonnull IPeripheral peripheral) {
			EntityMinecart minecart = access.getMinecart();
			update(minecart.getEntityWorld(), minecart.getPositionVector());

			if (peripheral instanceof ModemPeripheral) {
				ModemPeripheral modem = (ModemPeripheral) peripheral;
				if (modem.pollChanged()) {
					access.getData().setBoolean("active", modem.isActive());
					access.markDataDirty();
				}
			}
		}

		@Override
		public IPeripheral create(@Nonnull IMinecartAccess minecart) {
			return this;
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IMinecartAccess access) {
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
