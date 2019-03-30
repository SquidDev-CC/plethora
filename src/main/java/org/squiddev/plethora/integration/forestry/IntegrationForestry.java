package org.squiddev.plethora.integration.forestry;

import forestry.core.ModuleCore;
import forestry.core.config.Constants;
import forestry.core.items.ItemAlyzer;
import forestry.core.tiles.TileAnalyzer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;

@Mod.EventBusSubscriber(modid = PlethoraCore.ID)
public final class IntegrationForestry {
	public static final String analyzerMod = "forestry:analyzer";

	private static BasicModuleHandler analyzerCap;

	private IntegrationForestry() {
	}

	@Optional.Method(modid = Constants.MOD_ID)
	private static BasicModuleHandler getAnalyzerCap() {
		if (analyzerCap == null) {
			analyzerCap = new BasicModuleHandler(new ResourceLocation(analyzerMod), ModuleCore.getItems().portableAlyzer);
		}

		return analyzerCap;
	}

	@SubscribeEvent
	@Optional.Method(modid = Constants.MOD_ID)
	public static void attachCapabilitiesItem(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof ItemAlyzer) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, getAnalyzerCap());
		}
	}

	@SubscribeEvent
	@Optional.Method(modid = Constants.MOD_ID)
	public static void attachCapabilitiesTile(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof TileAnalyzer) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, getAnalyzerCap());
		}
	}
}
