package org.squiddev.plethora.api;

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
}
