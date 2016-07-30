package org.squiddev.plethora.core;

import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.core.docdump.CommandDump;

import static org.squiddev.plethora.core.PlethoraCore.*;

@Mod(modid = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, guiFactory = "org.squiddev.plethora.core.client.gui.GuiConfigCore")
public class PlethoraCore {
	public static final String ID = "Plethora-Core";
	public static final String NAME = "Plethora Core";
	public static final String VERSION = "${mod_version}";
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},)";

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigCore.init(event.getSuggestedConfigurationFile());

		MetaRegistry.instance.loadAsm(event.getAsmData());
		MethodRegistry.instance.loadAsm(event.getAsmData());
		ConverterRegistry.instance.loadAsm(event.getAsmData());
		CostHandler.register();

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
	public void onSeverStarting(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new CommandDump(evt.getServer().isDedicatedServer()));
	}

	@Mod.EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TaskHandler.reset();
		}
	}

	@Mod.EventHandler
	public void onMessageReceived(FMLInterModComms.IMCEvent event) {
		for (FMLInterModComms.IMCMessage m : event.getMessages()) {
			if (m.isStringMessage()) {
				if (Constants.IMC_BLACKLIST.equalsIgnoreCase(m.key)) {
					PeripheralProvider.addToBlacklist(m.getStringValue());
				}
			}
		}

	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			TaskHandler.update();
			CostHandler.update();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(PlethoraCore.ID)) {
			ConfigCore.sync();
		}
	}
}
