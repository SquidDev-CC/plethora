package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.CostHelpers;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static dan200.computercraft.core.apis.ArgumentHelper.getReal;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic;

/**
 * Various methods for mobs
 */
public final class MethodsKineticEntity {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S,
		target = EntityLivingBase.class,
		doc = "function(yaw:number, pitch:number) -- Look in a set direction"
	)
	@Nonnull
	public static MethodResult look(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final double yaw = getReal(args, 0) % 360;
		final double pitch = getReal(args, 1) % 360;

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				EntityLivingBase target = context.bake().getContext(EntityLivingBase.class);
				if (target instanceof EntityPlayerMP) {
					NetHandlerPlayServer handler = ((EntityPlayerMP) target).connection;
					handler.setPlayerLocation(target.posX, target.posY, target.posZ, (float) yaw, (float) pitch);
				} else {
					target.rotationYawHead = target.rotationYaw = target.renderYawOffset = (float) (yaw % 360);
					target.rotationPitch = (float) (pitch % 360);
				}
				return MethodResult.empty();
			}
		});
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityCreeper.class, worldThread = true,
		doc = "function() -- Explode this creeper"
	)
	public static Object[] explode(@Nonnull EntityCreeper target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) {
		target.explode();
		return null;
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityEnderman.class,
		doc = "function(x:number, y:number, z:number) -- Teleport to a position relative to the current one"
	)
	public static MethodResult teleport(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final double x = getReal(args, 0);
		final double y = getReal(args, 1);
		final double z = getReal(args, 2);

		assertBetween(x, -Kinetic.teleportRange, Kinetic.teleportRange, "X coordinate out of bounds (%s)");
		assertBetween(y, -Kinetic.teleportRange, Kinetic.teleportRange, "Y coordinate out of bounds (%s)");
		assertBetween(z, -Kinetic.teleportRange, Kinetic.teleportRange, "Z coordinate out of bounds (%s)");

		CostHelpers.checkCost(
			context.getCostHandler(),
			Math.sqrt(x * x + y * y + z * z) * Kinetic.teleportCost
		);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> baked = context.bake();

				EntityEnderman target = baked.getContext(EntityEnderman.class);
				return MethodResult.result(target.teleportTo(target.posX + x, target.posY + y, target.posZ + z));
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = AbstractSkeleton.class,
		doc = "function(potency:number) -- Fire an arrow in the direction the skeleton is looking"
	)
	@Nonnull
	public static MethodResult shoot(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull final Object[] args) throws LuaException {
		final double potency = getReal(args, 0);

		assertBetween(potency, 0.1, 1.0, "Potency out of range (%s).");

		CostHelpers.checkCost(unbaked.getCostHandler(), Kinetic.shootCost * potency);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> context = unbaked.bake();
				AbstractSkeleton skeleton = context.getContext(AbstractSkeleton.class);

				ItemStack stack = skeleton.getHeldItem(EnumHand.MAIN_HAND);
				if (stack.isEmpty() || stack.getItem() != Items.BOW) throw new LuaException("Not holding a bow");

				IWorldLocation location = context.getContext(IWorldLocation.class);

				EntityArrow arrow = (EntityArrow) ReflectionHelper
					.findMethod(AbstractSkeleton.class, null, new String[]{"func_190726_a", "getArrow"}, float.class)
					.invoke(skeleton, (float) potency);

				float rotationYaw = skeleton.rotationYaw;
				float rotationPitch = skeleton.rotationPitch;
				float motionX = (-MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
				float motionZ = (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
				float motionY = (-MathHelper.sin(rotationPitch / 180.0f * (float) Math.PI));

				arrow.setThrowableHeading(motionX, motionY, motionZ, 1.6f, (float) (potency * 2));

				double damage = potency * 2;
				int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
				if (power > 0) damage += power * 0.5 + 0.5;
				arrow.setDamage(damage);

				if (potency == 1.0) arrow.setIsCritical(true);

				skeleton.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRNG().nextFloat() * 0.4F + 0.8F));

				location.getWorld().spawnEntity(arrow);
				return MethodResult.empty();
			}
		});
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityMinecart.class,
		doc = "function(velocity:number) -- Propel this minecart in along the track."
	)
	public static MethodResult propel(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		double given = getReal(args, 0);

		assertBetween(given, -Kinetic.propelMax, Kinetic.propelMax, "Velocity coordinate out of bounds (%s)");

		CostHelpers.checkCost(
			context.getCostHandler(),
			Math.abs(given) * Kinetic.propelCost
		);

		// We provide a number * 10 as it seems more "friendly".
		final double velocity = given * 0.1;

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> baked = context.bake();

				EntityMinecart target = baked.getContext(EntityMinecart.class);
				double vx = target.motionX;
				double vz = target.motionZ;
				double len = Math.sqrt(vx * vx + vz * vz);

				// It isn't perfect, but it's better than nothing.
				if (len == 0) {
					float yaw = target.rotationYaw;
					vx = -MathHelper.sin(yaw / 180.0f * (float) Math.PI);
					vz = MathHelper.cos(yaw / 180.0f * (float) Math.PI);
					len = 1;
				}

				target.addVelocity(velocity * vx / len, 0, velocity * vz / len);
				target.velocityChanged = true;
				return MethodResult.empty();
			}
		});
	}
}

