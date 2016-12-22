package org.squiddev.plethora.integration.vanilla;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.core.BasicModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;

public class IntegrationVanilla {
	public static final String daylightSensor = "minecraft:daylight_detector";
	public static final String clock = "minecraft:clock";
	public static final String noteblock = "minecraft:noteblock";

	public static final ResourceLocation daylightSensorMod = new ResourceLocation(daylightSensor);
	public static final ResourceLocation clockMod = new ResourceLocation(clock);
	public static final ResourceLocation noteblockMod = new ResourceLocation(noteblock);

	private static BasicModuleHandler daylightSensorCap = new BasicModuleHandler(daylightSensor, Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR));
	private static BasicModuleHandler clockCap = new BasicModuleHandler(clock, Items.CLOCK);
	private static BasicModuleHandler noteblockCap = new BasicModuleHandler(noteblock, Item.getItemFromBlock(Blocks.NOTEBLOCK));

	public static void setup() {
		IntegrationVanilla instance = new IntegrationVanilla();
		MinecraftForge.EVENT_BUS.register(instance);
		DisableAI.register();
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation") // Use AttachCapabilities<T> in the future
	public void attachCapabilities(AttachCapabilitiesEvent.Item event) {
		Item item = event.getItem();
		if (item == Items.CLOCK) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, clockCap);
		} else if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block == Blocks.DAYLIGHT_DETECTOR) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, daylightSensorCap);
			} else if (block == Blocks.NOTEBLOCK) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, noteblockCap);
			}
		}
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void attachCapabilities(AttachCapabilitiesEvent.Entity event) {
		Entity entity = event.getEntity();
		if (entity instanceof EntityLiving) {
			event.addCapability(DisableAI.DISABLE_AI, new DisableAI.DefaultDisableAI());
		}
	}

	@SubscribeEvent
	public void entityTick(LivingEvent.LivingUpdateEvent event) {
		EntityLivingBase livingBase = event.getEntityLiving();
		if (livingBase instanceof EntityLiving) {
			DisableAI.maybeClear((EntityLiving) livingBase);
		}
	}
}
