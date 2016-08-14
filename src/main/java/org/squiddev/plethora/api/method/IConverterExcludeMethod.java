package org.squiddev.plethora.api.method;

/**
 * Marker interface stating this method should not consume converted objects.
 *
 * This will only target the original class, and not converted objects. This is useful if you will
 * do conversion yourself.
 *
 * Use {@link MarkerInterfaces} if attaching to a method.
 */
public interface IConverterExcludeMethod {
}
