package org.squiddev.plethora.api.meta;

import java.util.Map;

/**
 * A typed collection of metadata about an object.
 *
 * @param <T> The type about which we are capturing metadata.
 * @param <V> The value type for this metadata, conventionally {@code ?}.
 */
public interface TypedMeta<T, V> extends Map<String, V> {
}
