package org.squiddev.plethora.gameplay.modules;

import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class EntityLaser extends Entity implements IProjectile {
	private static final int TICKS_EXISTED = 30 * 20;

	@Nullable
	private EntityLivingBase shooter;
	@Nullable
	private EntityPlayer shooterPlayer;
	@Nullable
	private UUID shooterId;

	private float potency = 0.0f;

	public EntityLaser(World world) {
		super(world);
		setSize(0.25f, 0.25f);
	}

	@Override
	protected void entityInit() {
	}

	public EntityLaser(World world, EntityLivingBase shooter, float inaccuracy, float potency) {
		this(world);

		this.potency = potency;
		this.shooter = shooter;

		setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

		posX -= (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * 0.16f);
		posY -= 0.1;
		posZ -= (MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * 0.16f);
		setPosition(posX, posY, posZ);

		motionX = (-MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
		motionZ = (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
		motionY = (-MathHelper.sin(rotationPitch / 180.0f * (float) Math.PI));
		setThrowableHeading(motionX, motionY, motionZ, 1.5f, inaccuracy);
	}

	public void setPotency(float potency) {
		this.potency = potency;
	}

	@Override
	public void setThrowableHeading(double vx, double vy, double vz, float velocity, float inaccuracy) {
		// Normalise magnitude
		float magnitude = MathHelper.sqrt_double(vx * vx + vy * vy + vz * vz);
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

		float newMagnitude = MathHelper.sqrt_double(vx * vx + vz * vz);
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
			float magnitude = MathHelper.sqrt_double(x * x + z * z);
			prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * 180 / Math.PI);
			prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, magnitude) * 180 / Math.PI);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		if (shooterId == null && shooter instanceof EntityPlayer && !(shooter instanceof FakePlayer)) {
			shooterId = shooter.getPersistentID();
		}

		tag.setString("shooterId", shooterId == null ? "" : shooterId.toString());
		tag.setFloat("potency", potency);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		shooter = null;
		shooterId = null;
		shooterPlayer = null;

		String shooterName = tag.getString("shooterId");
		if (shooterName != null && shooterName.length() > 0) {
			try {
				shooterId = UUID.fromString(shooterName);
			} catch (IllegalArgumentException ignored) {
			}
		}

		potency = tag.getFloat("potency");
	}

	@Override
	public void onUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;

		super.onUpdate();

		Vec3d position = new Vec3d(posX, posY, posZ);
		Vec3d nextPosition = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

		RayTraceResult collision = worldObj.rayTraceBlocks(position, nextPosition);
		if (collision != null) nextPosition = collision.hitVec;

		if (!worldObj.isRemote) {
			List<Entity> collisions = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1, 1, 1));
			EntityLivingBase shooter = getShooter();

			double closestDistance = Double.POSITIVE_INFINITY;
			EntityLivingBase closestEntity = null;

			for (Entity other : collisions) {
				if (other.canBeCollidedWith() && (other != shooter || ticksExisted >= 5) && other instanceof EntityLivingBase) {
					float size = 0.3f;
					AxisAlignedBB singleCollision = other.getEntityBoundingBox().expand(size, size, size);
					RayTraceResult hit = singleCollision.calculateIntercept(position, nextPosition);

					if (hit != null) {
						double distanceSq = position.squareDistanceTo(hit.hitVec);
						if (distanceSq < closestDistance || closestDistance == 0.0) {
							closestEntity = (EntityLivingBase) other;
							closestDistance = distanceSq;
						}
					}
				}
			}

			if (closestEntity != null) {
				collision = new RayTraceResult(closestEntity);
			}
		}

		// Set position
		posX += motionX;
		posY += motionY;
		posZ += motionZ;

		setPosition(posX, posY, posZ);
		syncPositions();

		// Handle collision
		if (collision != null) {
			if (collision.typeOfHit == RayTraceResult.Type.BLOCK && worldObj.getBlockState(collision.getBlockPos()).getBlock() == Blocks.PORTAL) {
				setPortal(collision.getBlockPos());
			} else {
				onImpact(collision);
			}
		}

		if (!worldObj.isRemote && (potency < 0 || ticksExisted > TICKS_EXISTED)) {
			setDead();
		}
	}

	private void onImpact(RayTraceResult collision) {
		if (worldObj.isRemote) return;

		switch (collision.typeOfHit) {
			case BLOCK: {
				BlockPos position = collision.getBlockPos();
				World world = this.worldObj;

				IBlockState blockState = world.getBlockState(position);
				Block block = blockState.getBlock();
				if (!block.isAir(blockState, world, position) && !blockState.getMaterial().isLiquid()) {
					float hardness = blockState.getBlockHardness(world, position);
					if (hardness > -1 && hardness <= potency) {
						potency -= hardness;

						EntityPlayer player = getShooterPlayer();
						if (player != null) {
							if (!world.isBlockModifiable(player, position)) {
								potency = -1;
								return;
							}

							BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, position, blockState, player);
							MinecraftForge.EVENT_BUS.post(event);
							if (event.isCanceled()) {
								potency = -1;
								return;
							}
						}

						if (block == Blocks.TNT) {
							((BlockTNT) block).explode(
								world, position,
								blockState.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)),
								getShooter()
							);
						} else {
							List<ItemStack> drops = block.getDrops(world, position, blockState, 0);
							if (drops != null) {
								for (ItemStack stack : drops) {
									WorldUtil.dropItemStack(stack, world, position);
								}
							}
						}
						world.setBlockToAir(position);
					} else {
						potency = -1;
					}
				}
				break;
			}
			case ENTITY: {
				Entity entity = collision.entityHit;
				if (entity instanceof EntityLivingBase) {
					DamageSource source = new EntityDamageSourceIndirect("laser", this, getShooter()).setProjectile();
					entity.attackEntityFrom(source, potency * 3);
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
	private EntityLivingBase getShooter() {
		if (shooter != null) return shooter;

		if (!(worldObj instanceof WorldServer)) return null;
		WorldServer world = (WorldServer) worldObj;

		if (shooterId == null) {
			shooter = new PlethoraFakePlayer(world);
			syncPositions();
			return shooter;
		}

		Entity newShooter = world.getEntityFromUuid(shooterId);
		if (newShooter instanceof EntityLivingBase) {
			return shooter = (EntityLivingBase) newShooter;
		} else {
			return null;
		}
	}

	/**
	 * Get a player representing the shooter
	 *
	 * @return The player who shot it, a fake player if needed or {@code null}
	 */
	@Nullable
	private EntityPlayer getShooterPlayer() {
		if (shooterPlayer != null) return shooterPlayer;

		EntityLivingBase shooter = getShooter();
		if (shooter instanceof EntityPlayer) return shooterPlayer = (EntityPlayer) shooter;

		if (!(worldObj instanceof WorldServer)) return null;
		WorldServer world = (WorldServer) worldObj;

		shooterPlayer = new PlethoraFakePlayer(world);
		syncPositions();
		return shooterPlayer;
	}

	private void syncPositions() {
		EntityLivingBase shooter = this.shooter;

		if (shooter != null && shooter instanceof PlethoraFakePlayer) {
			shooter.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
		}

		if (shooterPlayer != null) {
			Entity from = shooter == null ? this : shooter;
			shooterPlayer.worldObj = from.worldObj;
			shooterPlayer.setPositionAndRotation(from.posX, from.posY, from.posZ, from.rotationYaw, from.rotationPitch);
		}
	}
}
