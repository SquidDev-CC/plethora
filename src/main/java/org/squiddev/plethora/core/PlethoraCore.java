package org.squiddev.plethora.core;

import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import static org.squiddev.plethora.core.PlethoraCore.*;

@Mod(modid = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES)
public class PlethoraCore {
	public static final String ID = "Plethora-Core";
	public static final String NAME = "Plethora Core";
	public static final String VERSION = "${mod_version}";
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},)";

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaRegistry.instance.loadAsm(event.getAsmData());
		MethodRegistry.instance.loadAsm(event.getAsmData());
		ConverterRegistry.instance.loadAsm(event.getAsmData());

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		ComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
	}

	@Mod.EventHandler
	public void onServerStart(FMLServerStartedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TaskHandler.reset();
		}
	}

	@Mod.EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TaskHandler.reset();
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			TaskHandler.update();
		}
	}
}
