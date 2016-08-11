package org.squiddev.plethora.integration.cctweaks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.utils.Helpers;

public class IntegrationCCTweaks {
	public static void setup() {
		if (Helpers.modLoaded("CCTweaks")) {
			IntegrationCCTweaks instance = new IntegrationCCTweaks();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Item event) {
		Item item = event.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block == Registry.blockNetworked) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new PeripheralHandlerWirelessBridge(event.getItemStack()));
			}
		}
	}
}
