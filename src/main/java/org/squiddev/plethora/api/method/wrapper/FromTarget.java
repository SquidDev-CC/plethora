package org.squiddev.plethora.api.method.wrapper;

import org.squiddev.plethora.api.method.ContextKeys;

import java.lang.annotation.*;

/**
 * Extract this value from the context's target, rather than taking it as an argument.
 *
 * This is just equivalent to {@link FromContext} with a context key of {@link ContextKeys#TARGET}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FromTarget {
}
