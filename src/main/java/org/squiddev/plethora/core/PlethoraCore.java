package org.squiddev.plethora.core;

import dan200.computercraft.api.ComputerCraftAPI;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
	}
}
