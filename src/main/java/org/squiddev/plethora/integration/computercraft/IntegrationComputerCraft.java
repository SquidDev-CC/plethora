package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.shared.peripheral.common.ItemPeripheralBase;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.utils.Helpers;

import static org.squiddev.plethora.integration.computercraft.WirelessModemPeripheralBase.MinecartUpgradeHandler;
import static org.squiddev.plethora.integration.computercraft.WirelessModemPeripheralBase.PeripheralHandler;

/**
 * Provides various peripherals for ComputerCraft items
 */
public class IntegrationComputerCraft {
	public static void setup() {
		if (Helpers.modLoaded("ComputerCraft")) {
			IntegrationComputerCraft instance = new IntegrationComputerCraft();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation") // Latest Forge uses a more generic method
	public void attachCapabilities(AttachCapabilitiesEvent.Item event) {
		ItemStack stack = event.getItemStack();

		if (event.getItem() instanceof ItemPeripheralBase) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new PeripheralCapabilityProvider(stack));
		}
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) {
		loadModel(event, "wireless_modem_off");
		loadModel(event, "wireless_modem_on");
		loadModel(event, "advanced_modem_off");
		loadModel(event, "advanced_modem_on");
	}

	private void loadModel(ModelBakeEvent event, String name) {
		IModel model = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation("computercraft", "block/" + name));
		IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		event.getModelRegistry().putObject(new ModelResourceLocation("computercraft:" + name, "inventory"), bakedModel);
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
		public boolean hasCapability(Capability<?> capability, EnumFacing enumFacing) {
			if (capability == Constants.PERIPHERAL_HANDLER_CAPABILITY) return getHandler() != null;
			if (capability == Constants.MINECART_UPGRADE_HANDLER_CAPABILITY) return getMinecart() != null;
			return false;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
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
							return peripheral = new PeripheralHandler(false, stack);
						case AdvancedModem:
							return peripheral = new PeripheralHandler(true, stack);
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
							return minecart = new MinecartUpgradeHandler(false, stack);
						case AdvancedModem:
							return minecart = new MinecartUpgradeHandler(true, stack);
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
