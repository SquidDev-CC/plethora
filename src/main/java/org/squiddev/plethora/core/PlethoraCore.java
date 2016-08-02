package org.squiddev.plethora.core;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.core.capabilities.DefaultCostHandler;
import org.squiddev.plethora.core.capabilities.DefaultModuleHandler;
import org.squiddev.plethora.core.capabilities.DefaultPeripheral;
import org.squiddev.plethora.core.capabilities.DefaultStorage;
import org.squiddev.plethora.core.docdump.CommandDump;

import static org.squiddev.plethora.core.PlethoraCore.*;

@Mod(modid = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, guiFactory = "org.squiddev.plethora.core.client.gui.GuiConfigCore")
public class PlethoraCore {
	public static final String ID = "Plethora-Core";
	public static final String NAME = "Plethora Core";
	public static final String VERSION = "${mod_version}";
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},)";

	private ASMDataTable asmData;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Setup the config file
		ConfigCore.init(event.getSuggestedConfigurationFile());

		asmData = event.getAsmData();

		// Register capabilities
		CapabilityManager.INSTANCE.register(ICostHandler.class, new DefaultStorage<ICostHandler>(), DefaultCostHandler.class);
		CapabilityManager.INSTANCE.register(IModuleHandler.class, new DefaultStorage<IModuleHandler>(), DefaultModuleHandler.class);
		CapabilityManager.INSTANCE.register(IPeripheral.class, new DefaultStorage<IPeripheral>(), DefaultPeripheral.class);
		CapabilityManager.INSTANCE.register(IPeripheralHandler.class, new DefaultStorage<IPeripheralHandler>(), DefaultPeripheral.class);

		// Various event handlers
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new PeripheralCapabilitiesProvider());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Preconditions.checkNotNull(asmData, "asmData table cannot be null: this means preInit was not fired");

		// Load various objects from annotations
		MetaRegistry.instance.loadAsm(asmData);
		MethodRegistry.instance.loadAsm(asmData);
		ConverterRegistry.instance.loadAsm(asmData);
		MethodTypeBuilder.instance.loadAsm(asmData);
	}

	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		ComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
	}

	@Mod.EventHandler
	public void onSeverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandDump(event.getServer().isDedicatedServer()));
	}

	@Mod.EventHandler
	public void onServerStart(FMLServerStartedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TaskHandler.reset();
			DefaultCostHandler.reset();
		}
	}

	@Mod.EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			TaskHandler.reset();
			DefaultCostHandler.reset();
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
			DefaultCostHandler.update();
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(PlethoraCore.ID)) {
			ConfigCore.sync();
		}
	}
}
