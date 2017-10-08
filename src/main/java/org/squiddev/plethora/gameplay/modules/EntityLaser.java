package org.squiddev.plethora.gameplay.modules;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.utils.WorldPosition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class EntityLaser extends Entity implements IProjectile {
	private static final int TICKS_EXISTED = 30 * 20;
	private static final Random random = new Random();

	@Nullable
	private Entity shooter;
	@Nullable
	private EntityPlayer shooterPlayer;
	@Nullable
	private UUID shooterId;

	@Nullable
	private WorldPosition shooterPos;

	private float potency = 0.0f;

	public EntityLaser(World world) {
		super(world);
		setSize(0.25f, 0.25f);
	}

	public EntityLaser(World world, Entity shooter, float inaccuracy, float potency) {
		this(world);

		this.potency = potency;
		setShooter(shooter);

		setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

		posX -= MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * 0.16f;
		posY -= 0.1;
		posZ -= MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * 0.16f;
		setPosition(posX, posY, posZ);

		motionX = -MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI);
		motionZ = MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI);
		motionY = -MathHelper.sin(rotationPitch / 180.0f * (float) Math.PI);
		setThrowableHeading(motionX, motionY, motionZ, 1.5f, inaccuracy);
	}

	public EntityLaser(World world, Vec3d shooter) {
		this(world);
		setShooter(new WorldPosition(world, shooter));
	}

	public EntityLaser(World world, BlockPos shooter) {
		this(world);
		this.shooterPos = new WorldPosition(world, shooter);
	}

	public void setShooter(@Nonnull Entity shooter) {
		this.shooter = shooter;
		this.shooterId = shooter.getPersistentID();
	}

	public void setShooter(WorldPosition position) {
		this.shooterPos = position;
	}

	@Override
	protected void entityInit() {
	}

	public void setPotency(float potency) {
		this.potency = potency;
	}

	@Override
	public void setThrowableHeading(double vx, double vy, double vz, float velocity, float inaccuracy) {
		// Normalise magnitude
		float magnitude = MathHelper.sqrt(vx * vx + vy * vy + vz * vz);
		vx /= magnitude;
		vy /= magnitude;
		vz /= magnitude;

		// Tiny offset
		vx += rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		vy += rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		vz += rand.nextGaussian() * 0.007499999832361937D * inaccuracy;

		// Reset velocity
		vx *= velocity;
		vy *= velocity;
		vz *= velocity;

		motionX = vx;
		motionY = vy;
		motionZ = vz;

		float newMagnitude = MathHelper.sqrt(vx * vx + vz * vz);
		prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(vx, vz) * 180 / Math.PI);
		prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(vy, newMagnitude) * 180 / Math.PI);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		motionX = x;
		motionY = y;
		motionZ = z;
		if (prevRotationPitch == 0.0f && prevRotationYaw == 0.0f) {
			float magnitude = MathHelper.sqrt(x * x + z * z);
			prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180 / Math.PI);
			prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, magnitude) * 180 / Math.PI);
		}
	}

	@Override
	public void writeEntityToNBT(@Nonnull NBTTagCompound tag) {
		if (shooterId != null) {
			tag.setString("shooterId", shooterId.toString());
		}

		if (shooterPos != null) {
			tag.setTag("shooterPos", shooterPos.serializeNBT());
		}

		tag.setFloat("potency", potency);
	}

	@Override
	public void readEntityFromNBT(@Nonnull NBTTagCompound tag) {
		shooter = null;
		shooterId = null;
		shooterPlayer = null;

		if (tag.hasKey("shooterId", 8)) {
			try {
				shooterId = UUID.fromString(tag.getString("shooterId"));
			} catch (IllegalArgumentException ignored) {
			}
		}

		if (tag.hasKey("shooterPos", 10)) {
			shooterPos = WorldPosition.deserializeNBT(tag.getCompoundTag("shooterPos"));
		}

		potency = tag.getFloat("potency");
	}

	@Override
	public void onUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;

		super.onUpdate();

		World worldObj = getEntityWorld();
		if (!worldObj.isRemote) {
			double remaining = 1;
			int ticks = 5; // Maximum of 5 steps. This limit should never be reached but you never know.

			// Raytrace to the next collision and set our position to there
			while (remaining >= 1e-2 && potency > 0 && --ticks >= 0) {
				Vec3d position = new Vec3d(posX, posY, posZ);
				Vec3d nextPosition = new Vec3d(
					posX + motionX * remaining,
					posY + motionY * remaining,
					posZ + motionZ * remaining
				);

				RayTraceResult collision = worldObj.rayTraceBlocks(position, nextPosition);
				if (collision != null) nextPosition = collision.hitVec;

				List<Entity> collisions = worldObj
					.getEntitiesWithinAABBExcludingEntity(this,
						getEntityBoundingBox()
							.addCoord(motionX * remaining, motionY * remaining, motionZ * remaining)
							.expand(1, 1, 1)
					);
				Entity shooter = getShooter();

				double closestDistance = nextPosition.squareDistanceTo(position);
				EntityLivingBase closestEntity = null;

				for (Entity other : collisions) {
					if (other.canBeCollidedWith() && (other != shooter || ticksExisted >= 5) && other instanceof EntityLivingBase) {
						if (
							other instanceof EntityPlayer && shooter instanceof EntityPlayer &&
								!((EntityPlayer) shooter).canAttackPlayer((EntityPlayer) other)
							) {
							continue;
						}

						float size = 0.3f;
						AxisAlignedBB singleCollision = other.getEntityBoundingBox().expand(size, size, size);
						RayTraceResult hit = singleCollision.calculateIntercept(position, nextPosition);

						if (hit != null) {
							double distanceSq = position.squareDistanceTo(hit.hitVec);
							if (distanceSq < closestDistance) {
								closestEntity = (EntityLivingBase) other;
								closestDistance = distanceSq;
								nextPosition = hit.hitVec;
							}
						}
					}
				}


				if (closestEntity != null) {
					collision = new RayTraceResult(closestEntity);
				}

				remaining -= position.distanceTo(nextPosition) / Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

				// Set position
				setPosition(nextPosition.xCoord, nextPosition.yCoord, nextPosition.zCoord);
				syncPositions(false);


				// Handle collision
				if (collision != null) {
					if (collision.typeOfHit == RayTraceResult.Type.BLOCK && worldObj.getBlockState(collision.getBlockPos()).getBlock() == Blocks.PORTAL) {
						setPortal(collision.getBlockPos());
					} else {
						onImpact(collision);
					}
				}
			}
		} else {
			// Set position
			posX += motionX;
			posY += motionY;
			posZ += motionZ;

			setPosition(posX, posY, posZ);
		}

		if (!worldObj.isRemote && (potency <= 0 || ticksExisted > TICKS_EXISTED)) {
			setDead();
		}
	}

	private void onImpact(RayTraceResult collision) {
		World world = getEntityWorld();
		if (world.isRemote) return;

		switch (collision.typeOfHit) {
			case BLOCK: {
				BlockPos position = collision.getBlockPos();

				IBlockState blockState = world.getBlockState(position);
				Block block = blockState.getBlock();
				if (!block.isAir(blockState, world, position) && !blockState.getMaterial().isLiquid()) {
					float hardness = blockState.getBlockHardness(world, position);

					EntityPlayer player = getShooterPlayer();
					if (player == null) return;

					// Ensure the player is setup correctly
					syncPositions(true);

					if (!world.isBlockModifiable(player, position)) {
						potency = -1;
						return;
					}

					if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, position, blockState, player))) {
						potency = -1;
						return;
					}

					if (block == Blocks.TNT) {
						potency -= hardness;

						// Ignite TNT blocks
						Entity shooter = getShooter();
						((BlockTNT) block).explode(
							world, position,
							blockState.withProperty(BlockTNT.EXPLODE, Boolean.TRUE),
							shooter instanceof EntityLivingBase ? (EntityLivingBase) shooter : getShooterPlayer()
						);

						world.setBlockToAir(position);
					} else if (block == Blocks.OBSIDIAN) {
						potency -= hardness;

						// Attempt to light obsidian blocks, creating a portal
						BlockPos offset = position.offset(collision.sideHit);
						IBlockState offsetState = world.getBlockState(offset);

						if (!offsetState.getBlock().isAir(offsetState, world, offset)) {
							return;
						}

						if (MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(new BlockSnapshot(world, position, offsetState), blockState, player, EnumHand.MAIN_HAND))) {
							return;
						}

						world.playSound(null, offset, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, random.nextFloat() * 0.4f + 0.8f);
						world.setBlockState(offset, Blocks.FIRE.getDefaultState());
					} else if (hardness > -1 && hardness <= potency) {
						potency -= hardness;

						List<ItemStack> drops = block.getDrops(world, position, blockState, 0);
						world.setBlockToAir(position);
						if (drops != null) {
							for (ItemStack stack : drops) {
								Block.spawnAsEntity(world, position, stack);
							}
						}
					} else {
						potency = -1;
					}
				}
				break;
			}
			case ENTITY: {
				Entity entity = collision.entityHit;
				if (entity instanceof EntityLivingBase) {
					// Ensure the player is setup correctly
					syncPositions(true);

					Entity shooter = getShooter();
					DamageSource source = shooter == null || shooter instanceof PlethoraFakePlayer ?
						new EntityDamageSource("laser", this) :
						new EntityDamageSourceIndirect("laser", this, shooter);

					source.setProjectile();

					entity.setFire(5);
					entity.attackEntityFrom(source, (float) (potency * ConfigGameplay.Laser.damage));
					potency = -1;
				}
				break;
			}
		}
	}

	/**
	 * Get the entity who shot the laser
	 *
	 * @return The entity who shot it, a fake player if needed or {@code null}
	 */
	@Nullable
	private Entity getShooter() {
		if (shooter != null) return shooter;

		World worldObj = getEntityWorld();
		if (!(worldObj instanceof WorldServer)) return null;
		WorldServer world = (WorldServer) worldObj;

		if (shooterId != null) {
			Entity newShooter = world.getEntityFromUuid(shooterId);
			return shooter = newShooter;
		}

		return shooter = shooterPlayer = new PlethoraFakePlayer(world);
	}

	/**
	 * Get a player representing the shooter
	 *
	 * @return The player who shot it, a fake player if needed or {@code null}
	 */
	@Nullable
	private EntityPlayer getShooterPlayer() {
		if (shooterPlayer != null) return shooterPlayer;

		Entity shooter = getShooter();
		if (shooter instanceof EntityPlayer) return shooterPlayer = (EntityPlayer) shooter;

		World worldObj = getEntityWorld();
		if (!(worldObj instanceof WorldServer)) return null;
		WorldServer world = (WorldServer) worldObj;

		return shooterPlayer = new PlethoraFakePlayer(world);
	}

	private void syncPositions(boolean force) {
		EntityPlayer fakePlayer = this.shooterPlayer;
		Entity shooter = this.shooter;
		if (!(fakePlayer instanceof PlethoraFakePlayer)) return;

		if (shooter != null && shooter != fakePlayer) {
			syncFromEntity(fakePlayer, shooter);
		} else if (shooterPos != null) {
			World current = fakePlayer.getEntityWorld();

			if (current == null || current.provider.getDimension() != shooterPos.getDimension()) {
				// Don't load another dimension unless we have to
				World replace = force ? shooterPos.getWorld(FMLCommonHandler.instance().getMinecraftServerInstance()) : shooterPos.getWorld();

				if (replace == null) {
					syncFromEntity(fakePlayer, this);
				} else {
					syncFromPos(fakePlayer, shooterPos.getPos(), rotationYaw, rotationPitch);
				}
			} else {
				syncFromPos(fakePlayer, shooterPos.getPos(), rotationYaw, rotationPitch);
			}
		} else {
			syncFromEntity(fakePlayer, this);
		}
	}

	private static void syncFromEntity(EntityPlayer player, Entity from) {
		player.setWorld(from.getEntityWorld());
		player.setPositionAndRotation(from.posX, from.posY, from.posZ, from.rotationYaw, from.rotationPitch);
	}

	private static void syncFromPos(EntityPlayer player, Vec3d pos, float yaw, float pitch) {
		player.setPositionAndRotation(pos.xCoord, pos.yCoord, pos.zCoord, yaw, pitch);
	}
}
