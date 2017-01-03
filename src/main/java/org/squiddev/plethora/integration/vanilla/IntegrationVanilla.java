package org.squiddev.plethora.integration.vanilla;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.tileentity.TileEntityNote;
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

	private static final BasicModuleHandler daylightSensorCap = new BasicModuleHandler(daylightSensor, Item.getItemFromBlock(Blocks.daylight_detector));
	private static final BasicModuleHandler clockCap = new BasicModuleHandler(clock, Items.clock);
	private static final BasicModuleHandler noteblockCap = new BasicModuleHandler(noteblock, Item.getItemFromBlock(Blocks.noteblock));

	public static void setup() {
		IntegrationVanilla instance = new IntegrationVanilla();
		MinecraftForge.EVENT_BUS.register(instance);
		DisableAI.register();
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.Item event) {
		Item item = event.getItem();
		if (item == Items.clock) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, clockCap);
		} else if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block == Blocks.daylight_detector) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, daylightSensorCap);
			} else if (block == Blocks.noteblock) {
				event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, noteblockCap);
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent.TileEntity event) {
		TileEntity entity = event.getTileEntity();
		if (entity instanceof TileEntityNote) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, noteblockCap);
		} else if (entity instanceof TileEntityDaylightDetector) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, daylightSensorCap);
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
