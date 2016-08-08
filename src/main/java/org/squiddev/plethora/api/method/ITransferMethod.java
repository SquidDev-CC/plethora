package org.squiddev.plethora.api.method;

/**
 * Marker interface stating this method requires transfer sources.
 *
 * You must put this on any method which consumes transfer sources:
 * otherwise things may not work as expected.
 *
 * Use {@link MarkerInterfaces} if attaching to a method.
 */
public interface ITransferMethod {
}
