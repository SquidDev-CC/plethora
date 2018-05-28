package org.squiddev.plethora.integration.forestry;

import com.google.common.base.Suppliers;
import forestry.core.ModuleCore;
import forestry.core.items.ItemAlyzer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;

import java.util.function.Supplier;

public class IntegrationForestry {
	public static final String analyzer = "forestry:analyzer";

	public static void setup() {
		MinecraftForge.EVENT_BUS.register(new IntegrationForestry());
	}

	@SubscribeEvent
	public void attachCapabilitiesItem(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof ItemAlyzer) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, analyzerCapProvider.get());
		}
	}

	private static final Supplier<BasicModuleHandler> analyzerCapProvider = Suppliers.memoize(() -> new BasicModuleHandler(
			new ResourceLocation(analyzer), ModuleCore.getItems().portableAlyzer
	));
}
