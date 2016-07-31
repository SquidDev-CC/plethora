package org.squiddev.plethora.core;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.common.ItemPeripheralBase;
import dan200.computercraft.shared.peripheral.modem.WirelessModemPeripheral;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;

/**
 * Provides various peripherals for ComputerCraft items
 */
public class PeripheralCapabilitiesProvider {
	private static final ResourceLocation PERIPHERAL_HANDLER_KEY = new ResourceLocation(Plethora.ID, "peripheralHandler");

	@SubscribeEvent
	public void onAttachItemCapabilities(AttachCapabilitiesEvent.Item event) {
		ItemStack stack = event.getItemStack();

		if (event.getItem() instanceof ItemPeripheralBase) {
			event.addCapability(PERIPHERAL_HANDLER_KEY, new PeripheralCapabilityProvider(stack));
		}
	}

	private static final class PeripheralCapabilityProvider implements ICapabilityProvider {
		private boolean checked = false;
		private IPeripheralHandler peripheral;
		private final ItemStack stack;

		private PeripheralCapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
			return capability == Constants.PERIPHERAL_HANDLER_CAPABILITY && getHandler() != null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
			return capability == Constants.PERIPHERAL_HANDLER_CAPABILITY ? (T) getHandler() : null;
		}

		private IPeripheralHandler getHandler() {
			if (checked) {
				return peripheral;
			} else {
				checked = true;

				if (stack.getItem() instanceof ItemPeripheralBase) {
					ItemPeripheralBase item = (ItemPeripheralBase) stack.getItem();
					switch (item.getPeripheralType(stack)) {
						case WirelessModem:
							return peripheral = new PeripheralHandlerModem(false, stack);
						case AdvancedModem:
							return peripheral = new PeripheralHandlerModem(true, stack);
						default:
							return null;
					}
				} else {
					return null;
				}
			}
		}
	}

	private static final class PeripheralHandlerModem extends WirelessModemPeripheral implements IPeripheralHandler {
		private final ItemStack stack;
		private World world;
		private Vec3 position;

		public PeripheralHandlerModem(boolean advanced, ItemStack stack) {
			super(advanced);
			this.stack = stack;
		}

		@Override
		protected World getWorld() {
			return world;
		}

		@Override
		protected Vec3 getPosition() {
			return world == null ? null : position;
		}

		@Override
		public boolean equals(IPeripheral other) {
			return this == other || (other instanceof PeripheralHandlerModem && stack == ((PeripheralHandlerModem) other).stack);
		}

		@Nonnull
		@Override
		public IPeripheral getPeripheral() {
			return this;
		}

		@Override
		public void update(@Nonnull World world, @Nonnull Vec3 position) {
			this.position = position;

			if (this.world != world) {
				this.world = world;
				switchNetwork();
			}
		}
	}
}
