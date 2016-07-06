package org.squiddev.plethora.gameplay;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.squiddev.plethora.integration.registry.Registry;

import static org.squiddev.plethora.gameplay.Plethora.*;

@Mod(modid = ID, name = NAME, version = VERSION, dependencies = DEPENDENCIES)
public class Plethora {
	public static final String ID = "Plethora";
	public static final String NAME = ID;
	public static final String VERSION = "${mod_version}";
	public static final String RESOURCE_DOMAIN = "plethora";
	public static final String DEPENDENCIES = "required-after:ComputerCraft@[${cc_version},);required-after:Plethora-Core";

	public static CreativeTabs getCreativeTab() {
		return tab;
	}

	@Mod.Instance(ID)
	public static Plethora instance;

	private static PlethoraCreativeTab tab;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		tab = new PlethoraCreativeTab();
		Registry.setup();
		Registry.preInit();
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

	public static class PlethoraCreativeTab extends CreativeTabs {
		public PlethoraCreativeTab() {
			super(RESOURCE_DOMAIN);
		}

		@Override
		public Item getTabIconItem() {
			return Registry.itemNeuralInterface;
		}
	}
}
