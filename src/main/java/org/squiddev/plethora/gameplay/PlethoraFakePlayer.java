package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.FakeNetHandler;

import java.util.WeakHashMap;

public class PlethoraFakePlayer extends FakePlayer {
	private static final WeakHashMap<Entity, PlethoraFakePlayer> registeredPlayers = new WeakHashMap<Entity, PlethoraFakePlayer>();

	private static final GameProfile profile = new GameProfile(Constants.FAKEPLAYER_UUID, "[" + Plethora.ID + "]");

	public PlethoraFakePlayer(WorldServer world) {
		super(world, profile);
		connection = new FakeNetHandler(this);
	}

	@Override
	public boolean canAttackPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public float getEyeHeight() {
		return 0.0F;
	}

	@Override
	public float getDefaultEyeHeight() {
		return 0.0F;
	}

	@Override
	public void dismountEntity(Entity entity) {
	}

	@Override
	public void openEditSign(TileEntitySign sign) {
	}

	@Override
	public Vec3d getPositionVector() {
		return new Vec3d(posX, posY, posZ);
	}

	public void load(EntityLivingBase from) {
		worldObj = from.worldObj;
		setPositionAndRotation(from.posX, from.posY, from.posZ, from.rotationYaw, from.rotationPitch);
		setSneaking(from.isSneaking());

		inventory.currentItem = 0;

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = from.getItemStackFromSlot(slot);

			if (stack != null) {
				setItemStackToSlot(slot, stack.copy());
				getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers(slot));
			} else {
				setItemStackToSlot(slot, null);
			}
		}

		inventory.markDirty();
	}

	public void unload(EntityLivingBase from) {
		inventory.currentItem = 0;

		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = getItemStackFromSlot(slot);
			from.setItemStackToSlot(slot, stack);
			if (stack != null) {
				getAttributeMap().removeAttributeModifiers(stack.getAttributeModifiers(slot));
			}
		}

		ItemStack[] main = inventory.mainInventory;
		IItemHandler handler = new EquipmentInvWrapper(from);
		for (int i = 1; i < main.length; i++) {
			ItemStack stack = main[i];
			for (int j = 0; j < 5; j++) {
				if (stack == null || stack.stackSize <= 0) break;
				stack = handler.insertItem(j, stack, false);
			}

			if (stack != null && stack.stackSize > 0) {
				dropItem(stack, true, false);
			}

			main[i] = null;
		}

		inventory.markDirty();
	}

	public static PlethoraFakePlayer getPlayer(WorldServer world, Entity entity) {
		PlethoraFakePlayer fake = registeredPlayers.get(entity);
		if (fake == null) {
			fake = new PlethoraFakePlayer(world);
			registeredPlayers.put(entity, fake);
		}

		return fake;
	}
}
