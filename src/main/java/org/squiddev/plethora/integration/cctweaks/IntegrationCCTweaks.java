package org.squiddev.plethora.integration.cctweaks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.cctweaks.CCTweaks;
import org.squiddev.cctweaks.core.registry.Registry;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.client.RenderHelpers;
import org.squiddev.plethora.utils.Helpers;

public class IntegrationCCTweaks {
	public static void setup() {
		if (Helpers.modLoaded(CCTweaks.ID)) {
			IntegrationCCTweaks instance = new IntegrationCCTweaks();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStack stack = event.getObject();
		Item item = stack.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block == Registry.blockNetworked) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new PeripheralHandlerWirelessBridge(stack));
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBakeEvent(ModelBakeEvent event) {
		RenderHelpers.loadModel(event, "cctweaks", "wireless_bridge_small");
	}
}
