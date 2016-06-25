package org.squiddev.plethora;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.squiddev.plethora.impl.ConverterRegistry;
import org.squiddev.plethora.impl.MetaRegistry;
import org.squiddev.plethora.impl.MethodRegistry;
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

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Registry.preInit();

		MetaRegistry.instance.loadAsm(event.getAsmData());
		MethodRegistry.instance.loadAsm(event.getAsmData());
		ConverterRegistry.instance.loadAsm(event.getAsmData());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Registry.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Registry.postInit();
	}
}
