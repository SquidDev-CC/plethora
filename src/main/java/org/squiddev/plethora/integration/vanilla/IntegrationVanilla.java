package org.squiddev.plethora.integration.vanilla;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.IBakedModel;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.minecart.MinecartModuleHandler;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.api.module.IModuleRegistry;
import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

public class IntegrationVanilla {
	public static final String daylightSensor = "minecraft:daylight_detector";
	public static final String clock = "minecraft:clock";
	public static final String noteblock = "minecraft:noteblock";

	public static final ResourceLocation daylightSensorMod = new ResourceLocation(daylightSensor);
	public static final ResourceLocation clockMod = new ResourceLocation(clock);
	public static final ResourceLocation noteblockMod = new ResourceLocation(noteblock);

	private static final MinecartModuleHandler daylightSensorCap = new MinecartModuleHandler(daylightSensorMod, Item.getItemFromBlock(Blocks.daylight_detector));
	private static final MinecartModuleHandler clockCap = new MinecartModuleHandler(clockMod, Items.clock);
	private static final MinecartModuleHandler noteblockCap = new MinecartModuleHandler(noteblockMod, Item.getItemFromBlock(Blocks.noteblock));

	public static void setup() {
		IntegrationVanilla instance = new IntegrationVanilla();
		MinecraftForge.EVENT_BUS.register(instance);
		DisableAI.register();

		IModuleRegistry registry = PlethoraAPI.instance().moduleRegistry();
		registry.registerPocketUpgrade(new ItemStack(Items.clock), "adjective.plethora.clock");
		registry.registerTurtleUpgrade(new ItemStack(Items.clock), "adjective.plethora.clock");

		registry.registerPocketUpgrade(new ItemStack(Blocks.daylight_detector), "adjective.plethora.daylight_detector");
		registry.registerTurtleUpgrade(new ItemStack(Blocks.daylight_detector), daylightSensorHandlerModel, "adjective.plethora.daylight_detector");

		registry.registerPocketUpgrade(new ItemStack(Blocks.noteblock), "adjective.plethora.note_block");
		registry.registerTurtleUpgrade(new ItemStack(Blocks.noteblock), noteblockHandlerModel, "adjective.plethora.note_block");
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

	private static final BasicModuleHandler daylightSensorHandlerModel = new BasicModuleHandler(daylightSensorMod, Item.getItemFromBlock(Blocks.daylight_detector)) {
		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Pair<IBakedModel, Matrix4f> getModel(float delta) {
			IBakedModel model = super.getModel(delta).getLeft();

			Matrix4f transform = new Matrix4f(
				0.6f, 0.0f, 0.0f, 0.2f,
				0.0f, 0.0f, 0.6f, 0.2f,
				0.0f, -0.6f, 0.0f, 0.6f,
				0.0f, 0.0f, 0.0f, 1.0f
			);
			// Rotate X -PI/2
			// Translate 0.2 0.2 0.6
			// Scale 0.6

			return Pair.of(model, transform);
		}
	};

	private static final BasicModuleHandler noteblockHandlerModel = new BasicModuleHandler(noteblockMod, Item.getItemFromBlock(Blocks.noteblock)) {
		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public Pair<IBakedModel, Matrix4f> getModel(float delta) {
			IBakedModel model = super.getModel(delta).getLeft();

			Matrix4f transform = new Matrix4f(
				0.6f, 0.0f, 0.0f, 0.2f,
				0.0f, 0.6f, 0.0f, 0.2f,
				0.0f, 0.0f, 0.6f, 0.3f,
				0.0f, 0.0f, 0.0f, 1.0f
			);

			// Translate 0.2 0.2 0.3
			// Scale 0.6

			return Pair.of(model, transform);
		}
	};
}
