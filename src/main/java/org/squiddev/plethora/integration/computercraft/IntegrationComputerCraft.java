package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.shared.peripheral.common.ItemPeripheralBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;

/**
 * Provides various peripherals for ComputerCraft items
 */
public class IntegrationComputerCraft {
	public static void setup() {
		if (Helpers.modLoaded("computercraft")) {
			IntegrationComputerCraft instance = new IntegrationComputerCraft();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation") // Latest Forge uses a more generic method
	public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();

		if (stack.getItem() instanceof ItemPeripheralBase) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new PeripheralCapabilityProvider(stack));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBakeEvent(ModelBakeEvent event) {
		RenderHelpers.loadModel(event, "computercraft", "wireless_modem_off");
		RenderHelpers.loadModel(event, "computercraft", "wireless_modem_on");
		RenderHelpers.loadModel(event, "computercraft", "advanced_modem_off");
		RenderHelpers.loadModel(event, "computercraft", "advanced_modem_on");
	}

	private static final class PeripheralCapabilityProvider implements ICapabilityProvider {
		private boolean checked = false;
		private IPeripheralHandler peripheral;
		private IMinecartUpgradeHandler minecart;
		private final ItemStack stack;

		private PeripheralCapabilityProvider(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing enumFacing) {
			if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return getHandler() != null;
			if (capability == Constants.MINECART_UPGRADE_HANDLER_CAPABILITY) return getMinecart() != null;
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing enumFacing) {
			if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return (T) getHandler();
			if (capability == Constants.MINECART_UPGRADE_HANDLER_CAPABILITY) return (T) getMinecart();
			return null;
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

		private IMinecartUpgradeHandler getMinecart() {
			if (checked) {
				return minecart;
			} else {
				checked = true;

				if (stack.getItem() instanceof ItemPeripheralBase) {
					ItemPeripheralBase item = (ItemPeripheralBase) stack.getItem();
					switch (item.getPeripheralType(stack)) {
						case WirelessModem:
							return minecart = new WirelessModemPeripheralBase.MinecartUpgradeHandler(false, stack);
						case AdvancedModem:
							return minecart = new WirelessModemPeripheralBase.MinecartUpgradeHandler(true, stack);
						case Speaker:
							return minecart = new SpeakerPeripheralBase.MinecartUpgradeHandler(stack);
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
