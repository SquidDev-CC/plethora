package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.Vec3;
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
		playerNetServerHandler = new FakeNetHandler(this);
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
	public void mountEntity(Entity entity) {
	}

	@Override
	public void dismountEntity(Entity entity) {
	}

	@Override
	public void openEditSign(TileEntitySign sign) {
	}

	@Override
	public Vec3 getPositionVector() {
		return new Vec3(posX, posY, posZ);
	}

	public void load(EntityLivingBase from) {
		worldObj = from.worldObj;
		setPositionAndRotation(from.posX, from.posY, from.posZ, from.rotationYaw, from.rotationPitch);
		setSneaking(from.isSneaking());

		inventory.currentItem = 0;

		for (int i = 0; i < 5; i++) {
			ItemStack stack = from.getEquipmentInSlot(i);

			if (stack != null) {
				setCurrentItemOrArmor(i, stack.copy());
				getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers());
			} else {
				setCurrentItemOrArmor(i, null);
			}
		}

		inventory.markDirty();
	}

	public void unload(EntityLivingBase from) {
		inventory.currentItem = 0;

		for (int i = 0; i < 5; i++) {
			ItemStack stack = getEquipmentInSlot(i);
			from.setCurrentItemOrArmor(i, stack);
			if (stack != null) {
				getAttributeMap().removeAttributeModifiers(stack.getAttributeModifiers());
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
