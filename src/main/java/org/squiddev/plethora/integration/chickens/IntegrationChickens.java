package org.squiddev.plethora.integration.chickens;

import com.setycz.chickens.ChickensMod;
import com.setycz.chickens.item.ItemAnalyzer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.core.PlethoraCore;

import javax.annotation.Nonnull;

public final class IntegrationChickens {
	private IntegrationChickens() {
	}

	/*The following classes from Chickens _may_ need added integration:
	 * Henhouse - Collects nearby chicken drops; uses hay bales as fuel, and creates dirt.
	 *            Standard inv methods should suffice, unless we want to reveal the 'energy' property (think furnace burn time).
	 * ItemLiquidEgg - In short, a single-use bucket.  Appears to be properly exposed by standard fluid capabilities.
	 * ItemColoredEgg - Custom egg for dye chickens; not a guaranteed spawn, same as vanilla eggs
	 * ItemSpawnEgg - The only metadata that may be useful for the eggs would be the species to spawn...
	 */

	public static final String ANALYZER_S = "chickens:analyzer";
	public static final ResourceLocation ANALYZER_MOD = new ResourceLocation(ANALYZER_S);

	public static void setup() {
		if (Loader.isModLoaded(ChickensMod.MODID)) {
			MinecraftForge.EVENT_BUS.register(new IntegrationChickens());
		}
	}

	private static BasicModuleHandler analyzerCap;

	@Optional.Method(modid = ChickensMod.MODID)
	private static BasicModuleHandler getAnalyzerCap() {
		if (analyzerCap == null) {
			analyzerCap = new BasicModuleHandler(ANALYZER_MOD, ChickensMod.analyzer);
		}

		return analyzerCap;
	}

	@SubscribeEvent
	@Optional.Method(modid = ChickensMod.MODID)
	public void attachCapabilitiesItem(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject().getItem() instanceof ItemAnalyzer) {
			event.addCapability(PlethoraCore.PERIPHERAL_HANDLER_KEY, getAnalyzerCap());
		}
	}

}
