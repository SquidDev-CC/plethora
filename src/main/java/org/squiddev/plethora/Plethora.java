package org.squiddev.plethora;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.squiddev.plethora.impl.ConverterRegistry;
import org.squiddev.plethora.impl.MetaRegistry;
import org.squiddev.plethora.impl.MethodRegistry;
import org.squiddev.plethora.neural.NeuralManager;
import org.squiddev.plethora.registry.Registry;

@Mod(modid = Plethora.ID, name = Plethora.NAME, version = Plethora.VERSION, dependencies = Plethora.DEPENDENCIES)
public class Plethora {
	public static final String ID = "Plethora";
	public static final String NAME = ID;
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = ID.toLowerCase();
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},);";

	public static CreativeTabs getCreativeTab() {
		return ComputerCraft.mainCreativeTab;
	}

	@Mod.Instance
	public static Plethora instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Registry.preInit();

		MetaRegistry.instance.loadAsm(event.getAsmData());
		MethodRegistry.instance.loadAsm(event.getAsmData());
		ConverterRegistry.instance.loadAsm(event.getAsmData());
		MinecraftForge.EVENT_BUS.register(new EventBus());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Registry.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Registry.postInit();
	}

	@EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event) {
		NeuralManager.setup();
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		NeuralManager.tearDown();
	}

	public class EventBus {
		@SubscribeEvent
		public void onServerTick(TickEvent.ServerTickEvent event) {
			if (event.phase == TickEvent.Phase.START) {
				NeuralManager.update();
			}
		}
	}
}
