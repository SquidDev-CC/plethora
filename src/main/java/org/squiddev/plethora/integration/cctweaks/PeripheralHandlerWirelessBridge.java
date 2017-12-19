package org.squiddev.plethora.integration.cctweaks;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.vehicle.IVehicleAccess;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.gameplay.client.RenderHelpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

public class PeripheralHandlerWirelessBridge implements IPeripheralHandler, IVehicleUpgradeHandler, IWorldPosition, ICapabilityProvider {
	private final ItemStack stack;
	private World world;
	private BlockPos pos;
	private Entity entity;
	private PocketBinding binding;

	@SideOnly(Side.CLIENT)
	private ModelResourceLocation model;

	public PeripheralHandlerWirelessBridge(ItemStack stack) {
		this.stack = stack;
	}

	@Nonnull
	@Override
	public IPeripheral getPeripheral() {
		if (binding == null) binding = new PocketBinding();
		return binding.getModem().modem;
	}

	@Override
	public void update(@Nonnull World world, @Nonnull Vec3d position, @Nullable EntityLivingBase entity) {
		update(world, position, (Entity) entity);
	}

	private void update(@Nonnull World world, @Nonnull Vec3d position, @Nullable Entity entity) {
		this.world = world;
		this.pos = new BlockPos(position);
		this.entity = entity;

		if (binding != null) binding.update();
	}

	@Override
	public IBlockAccess getBlockAccess() {
		return world;
	}

	@Nonnull
	@Override
	public BlockPos getPosition() {
		return pos;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing enumFacing) {
		if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return stack.getItemDamage() == 0;
		if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) return stack.getItemDamage() == 0;
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing enumFacing) {
		if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) {
			return stack.getItemDamage() == 0 ? (T) this : null;
		}
		if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) {
			return stack.getItemDamage() == 0 ? (T) this : null;
		}
		return null;
	}

	@Nonnull
	@Override
	public Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access) {
		if (model == null) model = new ModelResourceLocation("cctweaks:wireless_bridge_small", "inventory");

		ModelManager modelManager = RenderHelpers.getMesher().getModelManager();
		return Pair.of(modelManager.getModel(model), RenderHelpers.getIdentity());
	}

	@Override
	public void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral) {
		update(vehicle.getVehicle().getEntityWorld(), vehicle.getVehicle().getPositionVector(), vehicle.getVehicle());
	}

	@Nullable
	@Override
	public IPeripheral create(@Nonnull IVehicleAccess vehicle) {
		return getPeripheral();
	}

	private class PocketBinding extends NetworkBindingWithModem {
		public PocketBinding() {
			super(PeripheralHandlerWirelessBridge.this);
		}

		@Override
		public BindingModem createModem() {
			return new PocketModem();
		}

		@Override
		public PocketModem getModem() {
			return (PocketModem) modem;
		}

		@Override
		public void markDirty() {
			save();
		}

		@Override
		public void connect() {
			if (stack.hasTagCompound()) {
				load(stack.getTagCompound());
			}
			super.connect();
		}

		public void save() {
			if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
			save(stack.getTagCompound());
		}

		public void update() {
			// We may receive update events whilst not being attached. To prevent this, just exit if we
			// have no network
			if (getAttachedNetwork() == null) return;

			modem.updateEnabled();
			if (getModem().modem.pollChanged()) save();

			if (entity instanceof EntityPlayer) {
				((EntityPlayer) entity).inventory.markDirty();
			}
		}

		/**
		 * Custom modem that allows modifying bindings
		 */
		private class PocketModem extends BindingModem {
			@Override
			protected BasicModemPeripheral<?> createPeripheral() {
				return new PocketModemPeripheral(this);
			}
		}

		/**
		 * Calls {@link PocketBinding#connect()} and {@link PocketBinding#destroy()} on attach and detach.
		 */
		private class PocketModemPeripheral extends BindingModemPeripheral {
			public PocketModemPeripheral(NetworkBindingWithModem.BindingModem modem) {
				super(modem);
			}

			@Nonnull
			@Override
			public String[] getMethodNames() {
				String[] methods = super.getMethodNames();
				String[] newMethods = new String[methods.length + 2];
				System.arraycopy(methods, 0, newMethods, 0, methods.length);


				int l = methods.length;
				newMethods[l] = "bindFromCard";
				newMethods[l + 1] = "bindToCard";

				return newMethods;
			}

			private boolean loadFromCard(ItemStack stack) {
				if (stack != null && stack.getItem() instanceof IDataCard) {
					IDataCard card = (IDataCard) stack.getItem();
					if (PocketBinding.this.load(stack, card)) {
						PocketBinding.this.save();
						return true;
					}
				}

				return false;
			}

			@Override
			public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
				String[] methods = super.getMethodNames();
				switch (method - methods.length) {
					case 0: { // bindFromCard
						if (entity == null) {
							return new Object[]{false, "No inventory found"};
						} else if (entity instanceof EntityPlayer) {
							InventoryPlayer inventory = ((EntityPlayer) entity).inventory;

							int size = inventory.getSizeInventory(), held = inventory.currentItem;
							for (int i = 0; i < size; i++) {
								ItemStack stack = inventory.getStackInSlot((i + held) % size);
								if (loadFromCard(stack)) return new Object[]{true};
							}
						} else if (entity instanceof EntityLivingBase) {
							ItemStack stack = ((EntityLivingBase) entity).getHeldItem(EnumHand.MAIN_HAND);
							if (loadFromCard(stack)) return new Object[]{true};
						}

						return new Object[]{false, "No card found"};
					}
					case 1: { // bindToCard
						if (entity == null) {
							return new Object[]{false, "No inventory found"};
						} else if (entity instanceof EntityPlayer) {
							InventoryPlayer inventory = ((EntityPlayer) entity).inventory;

							int size = inventory.getSizeInventory(), held = inventory.currentItem;
							for (int i = 0; i < size; i++) {
								ItemStack stack = inventory.getStackInSlot((i + held) % size);
								if (!stack.isEmpty() && stack.getItem() instanceof IDataCard) {
									IDataCard card = (IDataCard) stack.getItem();
									PocketBinding.this.save(stack, card);
									return new Object[]{true};
								}
							}
						} else if (entity instanceof EntityLivingBase) {
							ItemStack stack = ((EntityLivingBase) entity).getHeldItem(EnumHand.MAIN_HAND);
							if (!stack.isEmpty() && stack.getItem() instanceof IDataCard) {
								IDataCard card = (IDataCard) stack.getItem();
								PocketBinding.this.save(stack, card);
								return new Object[]{true};
							}
						}

						return new Object[]{false, "No card found"};
					}
				}

				return super.callMethod(computer, context, method, arguments);
			}

			@Override
			public synchronized void attach(@Nonnull IComputerAccess computer) {
				PocketBinding.this.connect();
				super.attach(computer);
			}

			@Override
			public synchronized void detach(@Nonnull IComputerAccess computer) {
				super.detach(computer);
				PocketBinding.this.destroy();
			}
		}
	}
}
