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

public class IntegrationChickens {

	//REFINE Some integration classes have explicit private constructors,
	// others have implicit default public constructors...
	private IntegrationChickens() {
	}

	/*TODO List of items/entities/blocks from Chickens that _may_ need integration:
	 * Henhouse - Collects nearby chicken drops; uses hay bales as fuel, and creates dirt
	 * EntityChickensChicken
	 * ItemAnalyzer - Analyzes chickens in-world, at cost of durability
	 * ItemColoredEgg - Custom egg for dye chickens; not a guaranteed spawn, same as vanilla eggs
	 * ItemLiquidEgg - In short, a single-use bucket.  Check the LiquidEggFluidWrapper capability
	 * ItemSpawnEgg
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
