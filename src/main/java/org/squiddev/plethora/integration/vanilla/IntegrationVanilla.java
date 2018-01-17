package org.squiddev.plethora.integration.vanilla;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.api.module.IModuleRegistry;
import org.squiddev.plethora.api.vehicle.VehicleModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.core.modules.BasicModuleHandlerTransform;
import org.squiddev.plethora.core.modules.VehicleModuleHandlerTransform;

import javax.vecmath.Matrix4f;

public class IntegrationVanilla {
	public static final String daylightSensor = "minecraft:daylight_detector";
	public static final String clock = "minecraft:clock";
	public static final String noteblock = "minecraft:noteblock";

	public static final ResourceLocation daylightSensorMod = new ResourceLocation(daylightSensor);
	public static final ResourceLocation clockMod = new ResourceLocation(clock);
	public static final ResourceLocation noteblockMod = new ResourceLocation(noteblock);

	public static void setup() {
		IntegrationVanilla instance = new IntegrationVanilla();
		MinecraftForge.EVENT_BUS.register(instance);
		DisableAI.register();

		IModuleRegistry registry = PlethoraAPI.instance().moduleRegistry();
		registry.registerPocketUpgrade(new ItemStack(Items.CLOCK), "adjective.plethora.clock");
		registry.registerTurtleUpgrade(new ItemStack(Items.CLOCK), "adjective.plethora.clock");

		registry.registerPocketUpgrade(new ItemStack(Blocks.DAYLIGHT_DETECTOR), "adjective.plethora.daylight_detector");
		registry.registerTurtleUpgrade(new ItemStack(Blocks.DAYLIGHT_DETECTOR), daylightSensorHandlerModel, "adjective.plethora.daylight_detector");

		registry.registerPocketUpgrade(new ItemStack(Blocks.NOTEBLOCK), "adjective.plethora.note_block");
		registry.registerTurtleUpgrade(new ItemStack(Blocks.NOTEBLOCK), noteblockHandlerModel, "adjective.plethora.note_block");
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
	public void attachCapabilities(AttachCapabilitiesEvent.TileEntity event) {
		// We're meant to use AttachCapabilitiesEvent<TileEntity>, but leaving this for compat

		TileEntity entity = event.getTileEntity();
		if (entity instanceof TileEntityNote) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, noteblockCap);
		} else if (entity instanceof TileEntityDaylightDetector) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, daylightSensorCap);
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

	private static final BasicModuleHandler daylightSensorHandlerModel = new BasicModuleHandlerTransform(
		daylightSensorMod, Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR),
		new Matrix4f(
			0.0f, 0.6f, 0.0f, 0.2f,
			0.0f, 0.0f, 0.6f, -0.1f,
			0.6f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
		)
		// Scale 0.6
		// Rotate X -PI/2
		// Rotate Y -PI/2
		// Translate 0.2 -0.1 0
	);

	private static final BasicModuleHandler noteblockHandlerModel = new BasicModuleHandlerTransform(
		noteblockMod, Item.getItemFromBlock(Blocks.NOTEBLOCK),
		new Matrix4f(
			0.6f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.6f, 0.0f, -0.1f,
			0.0f, 0.0f, 0.6f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
		)
		// Scale 0.6
		// Translate 0 -0.1 0
	);

	private static final VehicleModuleHandler daylightSensorCap = new VehicleModuleHandlerTransform(
		daylightSensorMod,
		Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR),
		new Matrix4f(
			0.6f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f, -0.6f, 0.0f,
			0.0f, 0.6f, 0.0f, 0.2f,
			0.0f, 0.0f, 0.0f, 1.0f
		)
	);

	private static final VehicleModuleHandler clockCap = new VehicleModuleHandler(clockMod, Items.CLOCK);
	private static final VehicleModuleHandler noteblockCap = new VehicleModuleHandlerTransform(
		noteblockMod, Item.getItemFromBlock(Blocks.NOTEBLOCK),
		new Matrix4f(
			0.6f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.6f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.6f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f
		)
	);
}
