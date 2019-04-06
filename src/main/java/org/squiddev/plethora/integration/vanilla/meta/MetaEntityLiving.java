package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.method.ContextHelpers;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.method.LuaList;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic provider for living entities
 */
@Injects
public final class MetaEntityLiving extends BaseMetaProvider<EntityLivingBase> {
	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<EntityLivingBase> context) {
		EntityLivingBase target = context.getTarget();
		Map<String, Object> map = new HashMap<>();

		{
			Map<String, Object> armor = new HashMap<>();
			armor.put("boots", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.FEET)));
			armor.put("leggings", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.LEGS)));
			armor.put("chestplate", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.CHEST)));
			armor.put("helmet", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.HEAD)));
			map.put("armor", armor);
		}

		map.put("heldItem", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)));
		map.put("offhandItem", ContextHelpers.wrapStack(context, target.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)));
		map.put("potionEffects", LuaList.of(target.getActivePotionEffects(), PotionEffect::getEffectName).asMap());

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

	@Nonnull
	@Override
	public EntityLiving getExample() {
		EntityZombie entity = new EntityZombie(WorldDummy.INSTANCE);
		entity.setPositionAndRotation(12, 0, 0, 30, 73);
		entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
		entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_HELMET));
		return entity;
	}

}
