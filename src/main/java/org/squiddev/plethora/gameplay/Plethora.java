package org.squiddev.plethora.gameplay;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.squiddev.plethora.gameplay.client.ClientRegistration;
import org.squiddev.plethora.gameplay.keyboard.KeyMessage;
import org.squiddev.plethora.gameplay.keyboard.ListenMessage;
import org.squiddev.plethora.gameplay.keyboard.ServerKeyListener;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;
import org.squiddev.plethora.gameplay.minecart.MessageMinecartSlot;
import org.squiddev.plethora.gameplay.modules.ChatMessage;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasAdd;
import org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasRemove;
import org.squiddev.plethora.gameplay.modules.glasses.MessageCanvasUpdate;
import org.squiddev.plethora.gameplay.registry.BasicMessage;
import org.squiddev.plethora.gameplay.registry.Packets;
import org.squiddev.plethora.gameplay.registry.Registration;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.Plethora.*;
import static org.squiddev.plethora.gameplay.registry.Packets.*;

@Mod(modid = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, guiFactory = "org.squiddev.plethora.gameplay.client.gui.GuiConfigGameplay")
@Mod.EventBusSubscriber(modid = ID)
public class Plethora {
	public static final String ID = "plethora";
	public static final String NAME = "Plethora";
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = ID;
	public static final String DEPENDENCIES = "required-after:computercraft@[${cc_version},);required-after:plethora-core";

	public static final Logger LOG = LogManager.getLogger(ID);

	public static CreativeTabs getCreativeTab() {
		return tab;
	}

	@Mod.Instance(ID)
	public static Plethora instance;

	private static PlethoraCreativeTab tab;

	public static SimpleNetworkWrapper network;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		ConfigGameplay.init(event.getSuggestedConfigurationFile());

		// Set up entities
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "fakePlayer"), PlethoraFakePlayer.class, ID + ":fakePlayer", 1, instance, Integer.MAX_VALUE, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "laser"), EntityLaser.class, ID + ":laser", 0, instance, 64, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "minecartComputer"), EntityMinecartComputer.class, ID + ":minecartComputer", 2, instance, 80, 3, true);

		// Set up network
		network = NetworkRegistry.INSTANCE.newSimpleChannel(ID);

		network.registerMessage(BasicMessage::defaultHandler, ChatMessage.class, Packets.CHAT_MESSAGE, Side.CLIENT);
		network.registerMessage(BasicMessage::defaultHandler, MessageMinecartSlot.class, Packets.MINECART_MESSAGE, Side.CLIENT);
		network.registerMessage(BasicMessage::defaultHandler, KeyMessage.class, Packets.KEY_MESSAGE, Side.SERVER);
		network.registerMessage(BasicMessage::defaultHandler, ListenMessage.class, Packets.LISTEN_MESSAGE, Side.CLIENT);

		network.registerMessage(BasicMessage::defaultHandler, MessageCanvasAdd.class, CANVAS_ADD_MESSAGE, Side.CLIENT);
		network.registerMessage(BasicMessage::defaultHandler, MessageCanvasRemove.class, CANVAS_REMOVE_MESSAGE, Side.CLIENT);
		network.registerMessage(BasicMessage::defaultHandler, MessageCanvasUpdate.class, CANVAS_UPDATE_MESSAGE, Side.CLIENT);

		tab = new PlethoraCreativeTab();

		if (event.getSide() == Side.CLIENT) ClientRegistration.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		if (event.getSide() == Side.CLIENT) ClientRegistration.init();
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(ID)) {
			ConfigGameplay.sync();
		}
	}

	@EventHandler
	public static void onServerStopping(FMLServerStoppedEvent e) {
		ServerKeyListener.clear();
	}

	@EventHandler
	public static void onServerStarting(FMLServerStartedEvent e) {
		ServerKeyListener.clear();
	}

	private static class PlethoraCreativeTab extends CreativeTabs {
		public PlethoraCreativeTab() {
			super(RESOURCE_DOMAIN);
		}

		@Nonnull
		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack createIcon() {
			return new ItemStack(Registration.itemNeuralInterface);
		}
	}
}
