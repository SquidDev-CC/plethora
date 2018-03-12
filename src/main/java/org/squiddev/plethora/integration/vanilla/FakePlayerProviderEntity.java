package org.squiddev.plethora.integration.vanilla;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;

import java.util.WeakHashMap;

public final class FakePlayerProviderEntity {
	private static final WeakHashMap<Entity, PlethoraFakePlayer> registeredPlayers = new WeakHashMap<>();

	private FakePlayerProviderEntity() {
	}

	public static PlethoraFakePlayer getPlayer(Entity entity, IPlayerOwnable ownable) {
		return getPlayer(entity, ownable == null ? null : ownable.getOwningProfile());
	}

	public static PlethoraFakePlayer getPlayer(Entity entity, GameProfile profile) {
		PlethoraFakePlayer fake = registeredPlayers.get(entity);
		if (fake == null) {
			fake = new PlethoraFakePlayer((WorldServer) entity.getEntityWorld(), entity, profile);
			registeredPlayers.put(entity, fake);
		}

		return fake;
	}

	public static void load(PlethoraFakePlayer player, Entity entity) {
		player.setWorld(entity.getEntityWorld());
		player.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
		player.rotationYawHead = player.rotationYaw;
		player.setSize(entity.width, entity.height);
		player.eyeHeight = entity.height;

		player.setSneaking(entity.isSneaking());

		player.inventory.currentItem = 0;

		if (entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				ItemStack stack = living.getItemStackFromSlot(slot);

				if (!stack.isEmpty()) {
					player.setItemStackToSlot(slot, stack.copy());
					player.getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers(slot));
				} else {
					player.setItemStackToSlot(slot, ItemStack.EMPTY);
				}
			}
		}

		player.inventory.markDirty();
	}

	public static void unload(PlethoraFakePlayer player, EntityLivingBase entity) {
		player.inventory.currentItem = 0;
		player.setSize(0, 0);
		player.eyeHeight = player.getDefaultEyeHeight();

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = player.getItemStackFromSlot(slot);
			if (!ItemStack.areItemStacksEqual(stack, entity.getItemStackFromSlot(slot))) {
				entity.setItemStackToSlot(slot, stack);
			}

			if (!stack.isEmpty()) {
				player.getAttributeMap().removeAttributeModifiers(stack.getAttributeModifiers(slot));
			}
		}

		NonNullList<ItemStack> main = player.inventory.mainInventory;
		IItemHandler handler = new EquipmentInvWrapper(entity);
		for (int i = 1; i < main.size(); i++) {
			ItemStack stack = main.get(i);
			for (int j = 0; j < 5; j++) {
				if (stack.isEmpty()) break;
				stack = handler.insertItem(j, stack, false);
			}

			if (!stack.isEmpty()) {
				player.dropItem(stack, true, false);
			}

			main.set(i, ItemStack.EMPTY);
		}

		player.inventory.markDirty();
	}


}
