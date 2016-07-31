package org.squiddev.plethora.api;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.squiddev.plethora.api.method.ICostHandler;
import org.squiddev.plethora.api.module.IModuleHandler;

/**
 * Various constants for working with Plethora
 */
public class Constants {
	/**
	 * IMC command for blacklisting a tile entity or package (must have a trailing '.').
	 *
	 * Blacklisted tile entities will not be wrapped as peripherals
	 *
	 * Parameters: class name or package prefix
	 */
	public static final String IMC_BLACKLIST = "blacklistTileEntity";

	/**
	 * Capability for cost handlers.
	 *
	 * Provide this capability for custom cost handling.
	 *
	 * @see org.squiddev.plethora.api.method.IMethodRegistry#getCostHandler(ICapabilityProvider)
	 * @see ICostHandler
	 */
	@CapabilityInject(ICostHandler.class)
	public static Capability<ICostHandler> COST_HANDLER_CAPABILITY = null;

	/**
	 * Capability for modules.
	 *
	 * Provide this capability for an item to be a module
	 *
	 * @see IModuleHandler
	 */
	@CapabilityInject(IModuleHandler.class)
	public static Capability<IModuleHandler> MODULE_HANDLER_CAPABILITY = null;

	/**
	 * Capability for peripherals.
	 *
	 * Provide this capability for an object to be a peripheral. We register a peripheral provider which checks
	 * for this capability.
	 *
	 * @see IPeripheral
	 */
	@CapabilityInject(IPeripheral.class)
	public static Capability<IPeripheral> PERIPHERAL_CAPABILITY = null;

	/**
	 * Capability for peripheral handlers.
	 *
	 * Provide this capability for an object to be a peripheral with additional handling.
	 * We do not register a peripheral provider, nor do we automatically convert {@link IPeripheral}s
	 * to {@link IPeripheralHandler}s: you should provide both.
	 *
	 * @see IPeripheralHandler
	 */
	@CapabilityInject(IPeripheralHandler.class)
	public static Capability<IPeripheralHandler> PERIPHERAL_HANDLER_CAPABILITY = null;
}
