package org.squiddev.plethora.api;

/**
 * Thrown when the Plethora API cannot be loaded
 */
public class CoreNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -2084056436955095940L;

	CoreNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
