package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.common.ItemPeripheralBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.vehicle.IVehicleUpgradeHandler;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.client.RenderHelpers;

import javax.annotation.Nonnull;

/**
 * Provides various peripherals for ComputerCraft items
 */
@Mod.EventBusSubscriber(modid = PlethoraCore.ID)
public class IntegrationComputerCraft {
	@SubscribeEvent
	@Optional.Method(modid = ComputerCraft.MOD_ID)
	public static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();

		if (stack.getItem() instanceof ItemPeripheralBase) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new PeripheralCapabilityProvider(stack));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@Optional.Method(modid = ComputerCraft.MOD_ID)
	public static void onModelBakeEvent(ModelBakeEvent event) {
		RenderHelpers.loadModel(event, "computercraft", "wireless_modem_off");
		RenderHelpers.loadModel(event, "computercraft", "wireless_modem_on");
		RenderHelpers.loadModel(event, "computercraft", "advanced_modem_off");
		RenderHelpers.loadModel(event, "computercraft", "advanced_modem_on");
	}

	private static final class PeripheralCapabilityProvider implements ICapabilityProvider {
		private boolean checkedPeripheral = false;
		private IPeripheralHandler peripheral;
		private boolean checkedVehicle = false;
		private IVehicleUpgradeHandler vehicle;
		private final ItemStack stack;

		private PeripheralCapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing enumFacing) {
			if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return getHandler() != null;
			if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) return getVehicle() != null;
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing enumFacing) {
			if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return (T) getHandler();
			if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) return (T) getVehicle();
			return null;
		}

		private IPeripheralHandler getHandler() {
			if (checkedPeripheral) {
				return peripheral;
			} else {
				checkedPeripheral = true;

				if (stack.getItem() instanceof ItemPeripheralBase) {
					ItemPeripheralBase item = (ItemPeripheralBase) stack.getItem();
					switch (item.getPeripheralType(stack)) {
						case WirelessModem:
							return peripheral = new WirelessModemPeripheralBase.PeripheralHandler(false, stack);
						case AdvancedModem:
							return peripheral = new WirelessModemPeripheralBase.PeripheralHandler(true, stack);
						case Speaker:
							return peripheral = new SpeakerPeripheralBase.PeripheralHandler(stack);
						default:
							return null;
					}
				} else {
					return null;
				}
			}
		}

		private IVehicleUpgradeHandler getVehicle() {
			if (checkedVehicle) {
				return vehicle;
			} else {
				checkedVehicle = true;

				if (stack.getItem() instanceof ItemPeripheralBase) {
					ItemPeripheralBase item = (ItemPeripheralBase) stack.getItem();
					switch (item.getPeripheralType(stack)) {
						case WirelessModem:
							return vehicle = new WirelessModemPeripheralBase.VehicleUpgradeHandler(false, stack);
						case AdvancedModem:
							return vehicle = new WirelessModemPeripheralBase.VehicleUpgradeHandler(true, stack);
						case Speaker:
							return vehicle = new SpeakerPeripheralBase.VehicleUpgradeHandler(stack);
						default:
							return null;
					}
				} else {
					return null;
				}
			}
		}
	}
}
