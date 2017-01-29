package org.squiddev.plethora.integration.cctweaks;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
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
import org.squiddev.cctweaks.api.IDataCard;
import org.squiddev.cctweaks.api.IWorldPosition;
import org.squiddev.cctweaks.core.network.bridge.NetworkBindingWithModem;
import org.squiddev.cctweaks.core.network.modem.BasicModemPeripheral;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PeripheralHandlerWirelessBridge implements IPeripheralHandler, IWorldPosition, ICapabilityProvider {
	private final ItemStack stack;
	private World world;
	private BlockPos pos;
	private EntityLivingBase entity;
	private PocketBinding binding;

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
	public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
		return capability == Constants.PERIPHERAL_HANDLER_CAPABILITY && stack.getItemDamage() == 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		return capability == Constants.PERIPHERAL_HANDLER_CAPABILITY && stack.getItemDamage() == 0 ? (T) this : null;
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
			public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
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
						} else {
							ItemStack stack = entity.getHeldItem(EnumHand.MAIN_HAND);
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
								if (stack != null && stack.getItem() instanceof IDataCard) {
									IDataCard card = (IDataCard) stack.getItem();
									PocketBinding.this.save(stack, card);
									return new Object[]{true};
								}
							}
						} else {
							ItemStack stack = entity.getHeldItem(EnumHand.MAIN_HAND);
							if (stack != null && stack.getItem() instanceof IDataCard) {
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
			public synchronized void attach(IComputerAccess computer) {
				PocketBinding.this.connect();
				super.attach(computer);
			}

			@Override
			public synchronized void detach(IComputerAccess computer) {
				super.detach(computer);
				PocketBinding.this.destroy();
			}
		}
	}
}
