package org.squiddev.plethora.api.module;

import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.minecart.IMinecartUpgradeHandler;

import javax.annotation.Nonnull;

/**
 * Various helper methods for modules.
 *
 * You don't need to explicitly register a module as they are loaded through capabilities. However, additional features
 * should be registered.
 */
public interface IModuleRegistry {
	/**
	 * Register a turtle upgrade.
	 *
	 * This will use the stack's module handler and the item name + ".adjective" for its adjective.
	 *
	 * @param stack The stack containing the module.
	 */
	void registerTurtleUpgrade(@Nonnull ItemStack stack);

	/**
	 * Register a turtle upgrade.
	 *
	 * This will use the stack's module handler.
	 *
	 * @param stack     The stack containing the module.
	 * @param adjective The module's adjective.
	 */
	void registerTurtleUpgrade(@Nonnull ItemStack stack, @Nonnull String adjective);

	/**
	 * Register a turtle upgrade.
	 *
	 * @param stack     The stack containing the module.
	 * @param handler   The module handler.
	 * @param adjective The module's adjective.
	 */
	void registerTurtleUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective);

	/**
	 * Register a pocket upgrade. This only works if CCTweaks is installed.
	 *
	 * This will use the stack's module handler and the item name + ".adjective" for its adjective.
	 *
	 * @param stack The stack containing the module.
	 */
	void registerPocketUpgrade(@Nonnull ItemStack stack);

	/**
	 * Register a pocket upgrade. This only works if CCTweaks is installed.
	 *
	 * This will use the stack's module handler.
	 *
	 * @param stack     The stack containing the module.
	 * @param adjective The module's adjective.
	 */
	void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull String adjective);

	/**
	 * Register a pocket upgrade. This only works if CCTweaks is installed.
	 *
	 * @param stack     The stack containing the module.
	 * @param handler   The module handler.
	 * @param adjective The module's adjective.
	 */
	void registerPocketUpgrade(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective);

	/**
	 * Convert a module handler to a minecraft upgrade handler.
	 *
	 * @param handler The module handler to convert.
	 * @return The resulting minecraft upgrade handler.
	 */
	IMinecartUpgradeHandler toMinecartUpgrade(@Nonnull IModuleHandler handler);
}
