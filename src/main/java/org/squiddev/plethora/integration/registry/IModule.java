package org.squiddev.plethora.integration.registry;

/**
 * An item that can be registered
 */
public interface IModule {
	/**
	 * Can this module be loaded
	 *
	 * @return If this module should be loaded
	 */
	boolean canLoad();

	/**
	 * @see net.minecraftforge.fml.common.Mod.EventHandler
	 */
	void preInit();

	/**
	 * @see net.minecraftforge.fml.common.Mod.EventHandler
	 */
	void init();

	/**
	 * @see net.minecraftforge.fml.common.Mod.EventHandler
	 */
	void postInit();
}
