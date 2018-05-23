package org.squiddev.plethora.integration.vanilla.meta;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.ILuaObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.core.ContextFactory;
import org.squiddev.plethora.core.executor.BasicExecutor;
import org.squiddev.plethora.integration.MetaWrapper;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * A basic provider for living entities
 */
@IMetaProvider.Inject(EntityLivingBase.class)
public class MetaEntityLiving extends BaseMetaProvider<EntityLivingBase> {
	@Nonnull
	@Override
	public Map<Object, Object> getMeta(@Nonnull IPartialContext<EntityLivingBase> context) {
		EntityLivingBase target = context.getTarget();
		Map<Object, Object> map = Maps.newHashMap();

		{
			Map<String, Object> armor = Maps.newHashMap();
			armor.put("boots", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.FEET)));
			armor.put("leggings", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.LEGS)));
			armor.put("chestplate", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.CHEST)));
			armor.put("helmet", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
			map.put("armor", armor);
		}

		map.put("heldItem", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)));
		map.put("offhandItem", wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)));

		{
			Map<Object, String> potionEffects = Maps.newHashMap();
			@SuppressWarnings("unchecked")
			Collection<PotionEffect> effects = target.getActivePotionEffects();

			int count = 1;
			for (PotionEffect effect : effects) {
				potionEffects.put(count++, effect.getEffectName());
			}
			map.put("potionEffects", potionEffects);
		}

		map.put("health", target.getHealth());
		map.put("maxHealth", target.getMaxHealth());
		map.put("isAirborne", target.isAirBorne);
		map.put("isBurning", target.isBurning());
		map.put("isAlive", target.isEntityAlive());
		map.put("isInWater", target.isInWater());
		map.put("isOnLadder", target.isOnLadder());
		map.put("isSleeping", target.isPlayerSleeping());
		map.put("isRiding", target.isRiding());
		map.put("isSneaking", target.isSneaking());
		map.put("isSprinting", target.isSprinting());
		map.put("isWet", target.isWet());
		map.put("isChild", target.isChild());
		map.put("isDead", target.isDead);
		map.put("isElytraFlying", target.isElytraFlying());

		return map;
	}

	private static ILuaObject wrapStack(IPartialContext<?> context, ItemStack object) {
		if (object == null || object.isEmpty()) return null;

		MetaWrapper<ItemStack> wrapper = MetaWrapper.of(object.copy());
		if (context instanceof IContext) {
			return ((IContext<?>) context).makeChildId(wrapper).getObject();
		} else {
			return ContextFactory.of(wrapper).withExecutor(BasicExecutor.INSTANCE).getObject();
		}
	}
}
