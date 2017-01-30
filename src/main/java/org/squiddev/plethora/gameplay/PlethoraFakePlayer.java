package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.EquipmentInvWrapper;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.FakeNetHandler;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class PlethoraFakePlayer extends FakePlayer {
	private static final WeakHashMap<Entity, PlethoraFakePlayer> registeredPlayers = new WeakHashMap<Entity, PlethoraFakePlayer>();

	private static final GameProfile profile = new GameProfile(Constants.FAKEPLAYER_UUID, "[" + Plethora.ID + "]");

	private final WeakReference<Entity> owner;

	private BlockPos digPosition;
	private Block digBlock;

	private int currentDamage = -1;
	private int currentDamageState = -1;

	public PlethoraFakePlayer(WorldServer world, Entity owner) {
		super(world, profile);
		playerNetServerHandler = new FakeNetHandler(this);
		setSize(0, 0);
		this.owner = owner == null ? null : new WeakReference<Entity>(owner);
	}

	public PlethoraFakePlayer(WorldServer world, Entity owner, String name) {
		super(world, new GameProfile(Constants.FAKEPLAYER_UUID, name));
		playerNetServerHandler = new FakeNetHandler(this);
		setSize(0, 0);
		this.owner = owner == null ? null : new WeakReference<Entity>(owner);
	}

	public PlethoraFakePlayer(WorldServer world) {
		this(world, (Entity) null);
	}

	public PlethoraFakePlayer(WorldServer world, String name) {
		this(world, null, name);
	}

	@Override
	public boolean canAttackPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public float getDefaultEyeHeight() {
		return 0;
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
	public boolean isSilent() {
		return true;
	}

	@Override
	public void playSound(String name, float volume, float pitch) {
	}

	@Override
	public Vec3 getPositionVector() {
		return new Vec3(posX, posY, posZ);
	}

	public Pair<Boolean, String> attack(EntityLivingBase entity, Entity hitEntity) {
		if (hitEntity != null) {
			attackTargetEntityWithCurrentItem(hitEntity);
			return Pair.of(true, "entity");
		}

		return Pair.of(false, "Nothing to attack here");
	}

	private void setState(Block block, BlockPos pos) {
		theItemInWorldManager.cancelDestroyingBlock();
		theItemInWorldManager.durabilityRemainingOnBlock = -1;

		digPosition = pos;
		digBlock = block;
		currentDamage = -1;
		currentDamageState = -1;
	}

	public Pair<Boolean, String> dig(BlockPos pos, EnumFacing direction) {
		World world = worldObj;
		Block block = world.getBlockState(pos).getBlock();

		if (block != digBlock || !pos.equals(digPosition)) setState(block, pos);

		if (!world.isAirBlock(pos) && !block.getMaterial().isLiquid()) {
			if (block == Blocks.bedrock || block.getBlockHardness(world, pos) <= -1) {
				return Pair.of(false, "Unbreakable block detected");
			}

			ItemInWorldManager manager = theItemInWorldManager;
			for (int i = 0; i < 10; i++) {
				if (currentDamageState == -1) {
					manager.onBlockClicked(pos, direction.getOpposite());
					currentDamageState = manager.durabilityRemainingOnBlock;
				} else {
					currentDamage++;
					float hardness = block.getPlayerRelativeBlockHardness(this, world, pos) * (currentDamage + 1);
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
		newRotationYaw = rotationYawHead = rotationYaw;
		newRotationPitch = rotationPitch;
		setSize(from.width, from.height);
		eyeHeight = from.height;

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
		setSize(0, 0);
		eyeHeight = getDefaultEyeHeight();

		for (int i = 0; i < 5; i++) {
			ItemStack stack = getEquipmentInSlot(i);
			if (!ItemStack.areItemStacksEqual(stack, from.getEquipmentInSlot(i))) {
				from.setCurrentItemOrArmor(i, stack);
			}
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

	public Entity getOwner() {
		return owner == null ? null : owner.get();
	}

	public static PlethoraFakePlayer getPlayer(WorldServer world, Entity entity) {
		PlethoraFakePlayer fake = registeredPlayers.get(entity);
		if (fake == null) {
			fake = new PlethoraFakePlayer(world, entity);
			registeredPlayers.put(entity, fake);
		}

		return fake;
	}
}
