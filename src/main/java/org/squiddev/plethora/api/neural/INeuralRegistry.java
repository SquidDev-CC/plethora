package org.squiddev.plethora.api.neural;

import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Various methods for controlling interactions with the neural interface.
 */
public interface INeuralRegistry {
	/**
	 * Add an predicate which controls whether an entity can have
	 * the neural interface attached to it.
	 *
	 * @param predicate The predicate which will recieve an entity. If {@code false} is returned then the neural
	 *                  interface will be blocked from connecting, otherwise the remaining predicates will be checked.
	 */
	void addEquipPredicate(@Nonnull Predicate<EntityLivingBase> predicate);
}
