package org.squiddev.plethora.api.module;

import org.squiddev.plethora.api.reference.IReference;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Some default implementations for {@link IModuleHandler} methods.
 *
 * It is recommended that all implementations extend this class: it allows additional methods
 * to be added to the module handler without introducing abstract method errors occurring.
 */
public abstract class AbstractModuleHandler implements IModuleHandler {
	@Nonnull
	@Override
	public Collection<IReference<?>> getAdditionalContext() {
		return Collections.emptyList();
	}
}
