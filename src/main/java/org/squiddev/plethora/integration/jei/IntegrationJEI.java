package org.squiddev.plethora.integration.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;

/**
 * Adds a usage handler which tells you where peripherals/modules can be used
 */
@JEIPlugin
public class IntegrationJEI extends BlankModPlugin {
	@Override
	public void register(@Nonnull IModRegistry registry) {
		ModulesWrapper.setup(registry);
		PeripheralsWrapper.setup(registry);
	}
}
