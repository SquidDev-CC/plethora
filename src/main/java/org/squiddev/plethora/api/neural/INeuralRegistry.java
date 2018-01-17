package org.squiddev.plethora.api.neural;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nonnull;

/**
 * Various methods for controlling interactions with the neural interface.
 */
public interface INeuralRegistry {
	/**
	 * Add an predicate which controls whether an entity can have
	 * the neural interface attached to it.
	 *
	 * @param predicate The predicate which will receive an entity. If {@code false} is returned then the neural
	 *                  interface will be blocked from connecting, otherwise the remaining predicates will be checked.
	 */
	void addEquipPredicate(@Nonnull Predicate<EntityLivingBase> predicate);
}
