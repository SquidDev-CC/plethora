package org.squiddev.plethora.registry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A module that adds custom client functionality
 */
public interface IClientModule extends IModule {
	/**
	 * Register custom handlers on the client
	 */
	@SideOnly(Side.CLIENT)
	void clientInit();

	/**
	 * Register custom handlers on the client
	 */
	@SideOnly(Side.CLIENT)
	void clientPreInit();
}
