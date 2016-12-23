package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.FakeNetHandler;

import java.util.WeakHashMap;

public class PlethoraFakePlayer extends FakePlayer {
	private static final WeakHashMap<Entity, PlethoraFakePlayer> registeredPlayers = new WeakHashMap<Entity, PlethoraFakePlayer>();

	private static final GameProfile profile = new GameProfile(Constants.FAKEPLAYER_UUID, "[" + Plethora.ID + "]");

	private BlockPos digPosition;
	private Block digBlock;

	private int currentDamage = -1;
	private int currentDamageState = -1;

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

	public Pair<Boolean, String> attack(EntityLivingBase entity, Entity hitEntity) {
		if (hitEntity != null) {
			attackTargetEntityWithCurrentItem(hitEntity);
			return Pair.of(true, "entity");
		}

		return Pair.of(false, "Nothing to attack here");
	}

	private void setState(Block block, BlockPos pos) {
		interactionManager.cancelDestroyingBlock();
		interactionManager.durabilityRemainingOnBlock = -1;

		digPosition = pos;
		digBlock = block;
		currentDamage = -1;
		currentDamageState = -1;
	}

	public Pair<Boolean, String> dig(BlockPos pos, EnumFacing direction) {
		World world = worldObj;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block != digBlock || !pos.equals(digPosition)) setState(block, pos);

		if (!world.isAirBlock(pos) && !state.getMaterial().isLiquid()) {
			if (block == Blocks.BEDROCK || state.getBlockHardness(world, pos) <= -1) {
				return Pair.of(false, "Unbreakable block detected");
			}

			PlayerInteractionManager manager = interactionManager;
			for (int i = 0; i < 10; i++) {
				if (currentDamageState == -1) {
					manager.onBlockClicked(pos, direction.getOpposite());
					currentDamageState = manager.durabilityRemainingOnBlock;
				} else {
					currentDamage++;
					float hardness = state.getPlayerRelativeBlockHardness(this, world, pos) * (currentDamage + 1);
					int hardnessState = (int) (hardness * 10);

					if (hardnessState != currentDamageState) {
						world.sendBlockBreakProgress(getEntityId(), pos, hardnessState);
						currentDamageState = hardnessState;
					}

					if (hardness >= 1) {
						manager.tryHarvestBlock(pos);

						setState(null, null);
						break;
					}
				}
			}

			return Pair.of(true, "block");
		}

		return Pair.of(false, "Nothing to dig here");
	}

	public void load(EntityLivingBase from) {
		worldObj = from.worldObj;
		setPositionAndRotation(from.posX, from.posY, from.posZ, from.rotationYaw, from.rotationPitch);
		rotationYawHead = rotationYaw;
		height = from.height;

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
			if (!ItemStack.areItemStacksEqual(stack, from.getItemStackFromSlot(slot))) {
				from.setItemStackToSlot(slot, stack);
			}

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
