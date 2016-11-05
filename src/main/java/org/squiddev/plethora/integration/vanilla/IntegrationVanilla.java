package org.squiddev.plethora.integration.vanilla;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.core.BasicModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;

public class IntegrationVanilla {
	public static final String daylightSensor = "minecraft:daylightSensor";
	public static final String clock = "minecraft:clock";

	public static void setup() {
		IntegrationVanilla instance = new IntegrationVanilla();
		MinecraftForge.EVENT_BUS.register(instance);
		DisableAI.register();
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Item event) {
		Item item = event.getItem();
		if (item == Items.clock) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new BasicModuleHandler(clock, event.getItemStack()));
		} else if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block == Blocks.daylight_detector) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, new BasicModuleHandler(daylightSensor, event.getItemStack()));
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Entity event) {
		Entity entity = event.getEntity();
		if (entity instanceof EntityLiving) {
			event.addCapability(DisableAI.DISABLE_AI, new DisableAI.DefaultDisableAI());
		}
	}

	@SubscribeEvent
	public void entityTick(LivingEvent.LivingUpdateEvent event) {
		EntityLivingBase livingBase = event.entityLiving;
		if (livingBase instanceof EntityLiving) {
			DisableAI.maybeClear((EntityLiving) livingBase);
		}
	}
}
