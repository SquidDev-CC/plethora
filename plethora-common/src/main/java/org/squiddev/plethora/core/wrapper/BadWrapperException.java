package org.squiddev.plethora.core.wrapper;

final class BadWrapperException extends RuntimeException {
	public static final BadWrapperException INSTANCE = new BadWrapperException("Error generating method wrapper");

	private static final long serialVersionUID = -1429222867771289808L;

	private BadWrapperException(String message) {
		super(message, null, true, false);
	}
}
