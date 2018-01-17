package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.speaker.SpeakerPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
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

public class SpeakerPeripheralBase extends SpeakerPeripheral {
	private World world;
	private Vec3d position;

	public void update(@Nonnull World world, @Nonnull Vec3d position) {
		update();
		this.position = position;
		this.world = world;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public BlockPos getPos() {
		return new BlockPos(position);
	}

	public static final class PeripheralHandler extends SpeakerPeripheralBase implements IPeripheralHandler {
		private final ItemStack stack;

		public PeripheralHandler(ItemStack stack) {
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

	public static final class VehicleUpgradeHandler extends SpeakerPeripheralBase implements IVehicleUpgradeHandler {
		private final ItemStack stack;

		public VehicleUpgradeHandler(ItemStack stack) {
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
		}

		@Override
		public IPeripheral create(@Nonnull IVehicleAccess vehicle) {
			return this;
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access) {
			// Scale 0.6
			// RotateX PI
			// Translate 0 -0.1 0
			return Pair.of(
				RenderHelpers.getMesher().getItemModel(stack),
				new Matrix4f(
					0.6f, 0.0f, 0.0f, 0.0f,
					0.0f, -0.6f, 0.0f, 0.0f,
					0.0f, 0.0f, -0.6f, -0.5f + (1 / 32.0f),
					0.0f, 0.0f, 0.0f, 1.0f
				)
			);
		}
	}
}
