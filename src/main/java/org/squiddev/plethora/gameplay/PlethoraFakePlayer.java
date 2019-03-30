package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.FakeNetHandler;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class PlethoraFakePlayer extends FakePlayer {
	public static final GameProfile PROFILE = new GameProfile(Constants.FAKEPLAYER_UUID, "[" + Plethora.ID + "]");

	private final WeakReference<Entity> owner;

	private BlockPos digPosition;
	private Block digBlock;

	private int currentDamage = -1;
	private int currentDamageState = -1;

	public PlethoraFakePlayer(WorldServer world, Entity owner, GameProfile profile) {
		super(world, profile != null && profile.isComplete() ? profile : PROFILE);
		connection = new FakeNetHandler(this);
		setSize(0, 0);
		if (owner != null) {
			setCustomNameTag(owner.getName());
			this.owner = new WeakReference<>(owner);
		} else {
			this.owner = null;
		}
	}

	@Deprecated
	public PlethoraFakePlayer(World world) {
		super((WorldServer) world, PROFILE);
		owner = null;
	}

	@Nonnull
	@Override
	protected HoverEvent getHoverEvent() {
		NBTTagCompound tag = new NBTTagCompound();
		Entity owner = getOwner();
		if (owner != null) {
			tag.setString("id", owner.getCachedUniqueIdString());
			tag.setString("name", owner.getName());
			ResourceLocation type = EntityList.getKey(owner);
			if (type != null) tag.setString("type", type.toString());
		} else {
			tag.setString("id", getCachedUniqueIdString());
			tag.setString("name", getName());
		}

		return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(tag.toString()));
	}


	public Entity getOwner() {
		return owner == null ? null : owner.get();
	}

	//region FakePlayer overrides
	@Override
	public void addStat(StatBase stat, int count) {
		MinecraftServer server = world.getMinecraftServer();
		if (server != null && getGameProfile() != PROFILE) {
			EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(getUniqueID());
			if (player != null) player.addStat(stat, count);
		}
	}

	@Override
	public Vec3d getPositionVector() {
		return new Vec3d(posX, posY, posZ);
	}


	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
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
	public void playSound(@Nonnull SoundEvent soundIn, float volume, float pitch) {
	}

	public void updateCooldown() {
		ticksSinceLastSwing = 20;
	}
	//endregion

	//region Dig
	private void setState(Block block, BlockPos pos) {
		interactionManager.cancelDestroyingBlock();
		interactionManager.durabilityRemainingOnBlock = -1;

		digPosition = pos;
		digBlock = block;
		currentDamage = -1;
		currentDamageState = -1;
	}

	public Pair<Boolean, String> dig(BlockPos pos, EnumFacing direction) {
		World world = getEntityWorld();
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
	//endregion
}
