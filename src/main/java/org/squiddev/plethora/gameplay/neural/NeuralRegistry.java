package org.squiddev.plethora.gameplay.neural;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.squiddev.plethora.api.neural.INeuralRegistry;

import javax.annotation.Nonnull;
import java.util.List;

public class NeuralRegistry implements INeuralRegistry {
	public static final NeuralRegistry instance = new NeuralRegistry();

	private final List<Predicate<EntityLivingBase>> predicates = Lists.newArrayList();

	private NeuralRegistry() {
	}

	@Override
	public void addEquipPredicate(@Nonnull Predicate<EntityLivingBase> predicate) {
		Preconditions.checkNotNull(predicate, "predicates cannot be null");
		predicates.add(predicate);
	}

	public boolean canEquip(@Nonnull EntityLivingBase entity) {
		if (entity.isChild() || entity instanceof EntityPlayer) return false;

		for (Predicate<EntityLivingBase> pred : predicates) {
			if (!pred.apply(entity)) return false;
		}

		return true;
	}
}
