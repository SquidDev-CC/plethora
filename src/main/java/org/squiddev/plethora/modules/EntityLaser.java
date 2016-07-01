package org.squiddev.plethora.modules;

import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public final class EntityLaser extends EntityThrowable {
	private float potency = 0.0f;

	public EntityLaser(World world) {
		super(world);
	}

	public EntityLaser(World world, EntityLivingBase shooter, float inaccuracy, float potency) {
		super(world, shooter);
		this.potency = potency;

		setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

		posX -= (double) (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * 0.16f);
		posY -= 0.1;
		posZ -= (double) (MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * 0.16f);
		setPosition(posX, posY, posZ);

		motionX = (double) (-MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
		motionZ = (double) (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
		motionY = (double) (-MathHelper.sin(rotationPitch / 180.0f * (float) Math.PI));
		setThrowableHeading(motionX, motionY, motionZ, 1.5f, inaccuracy);
	}

	public void setPotency(float potency) {
		this.potency = potency;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setFloat("potency", potency);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		potency = tag.getFloat("potency");
	}

	@Override
	protected float getGravityVelocity() {
		return 0;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote && (potency < 0 || ticksExisted > 20 * 30)) {
			setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition collision) {
		if (worldObj.isRemote) return;

		switch (collision.typeOfHit) {
			case BLOCK: {
				BlockPos position = collision.getBlockPos();
				World world = this.worldObj;

				IBlockState blockState = world.getBlockState(position);
				Block block = blockState.getBlock();
				if (!block.isAir(world, position) && !block.getMaterial().isLiquid() && block != Blocks.bed) {
					float hardness = block.getBlockHardness(world, position);
					if (hardness > -1 && hardness <= potency) {
						potency -= hardness;

						// We *should* fire events but that requires creating a fake player.
						List<ItemStack> drops = block.getDrops(world, position, blockState, 0);
						if (drops != null) {
							for (ItemStack stack : drops) {
								WorldUtil.dropItemStack(stack, world, position);
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

					DamageSource source = new EntityDamageSourceIndirect("laser", this, getThrower()).setProjectile();
					entity.attackEntityFrom(source, potency * 3);
					potency = -1;
				}
				break;
			}
		}
	}
}
