package org.squiddev.plethora.core.gen;

class BadWrapperException extends RuntimeException {
	public static final BadWrapperException INSTANCE = new BadWrapperException("Error generating method wrapper");

	public BadWrapperException(String message) {
		super(message, null, true, false);
	}
}
