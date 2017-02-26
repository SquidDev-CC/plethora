package org.squiddev.plethora.api;

import org.squiddev.plethora.api.neural.INeuralRegistry;

/**
 * API entry point for Plethora
 */
public class PlethoraGameplayAPI {
	public interface IPlethoraGameplayAPI {
		INeuralRegistry neuralRegistry();
	}

	private static final IPlethoraGameplayAPI API;

	/**
	 * Get the main API entry point
	 *
	 * @return Main API entry point
	 */
	public static IPlethoraGameplayAPI instance() {
		return API;
	}

	static {
		IPlethoraGameplayAPI api;
		final String name = "org.squiddev.plethora.gameplay.API";
		try {
			Class<?> registryClass = Class.forName(name);
			api = (IPlethoraGameplayAPI) registryClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new CoreNotFoundException("Cannot load Plethora Gameplay API as " + name + " cannot be found", e);
		} catch (InstantiationException e) {
			throw new CoreNotFoundException("Cannot load Plethora Gameplay API as " + name + " cannot be created", e);
		} catch (IllegalAccessException e) {
			throw new CoreNotFoundException("Cannot load Plethora Gameplay API as " + name + " cannot be accessed", e);
		}
		API = api;
	}

}
