package org.squiddev.plethora.api.method.wrapper;

import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.ISubTargetedMethod;

import java.lang.annotation.*;

/**
 * Extract this value from the context's target, rather than taking it as an argument.
 *
 * This is largely similar to {@link FromContext}, though is more selective in where it extracts the entry, and the
 * type will be used as a {@link ISubTargetedMethod#getSubTarget()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface FromSubtarget {
	/**
	 * The context key to extract from. When blank, this will be equivalent to {@link ContextKeys#ORIGIN} and then
	 * all modules you may be attached to.
	 */
	String[] value() default {};
}
