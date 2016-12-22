package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;
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
		final double yaw = ArgumentHelper.getNumber(args, 0);
		final double pitch = ArgumentHelper.getNumber(args, 1);

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
		final double x = getNumber(args, 0);
		final double y = getNumber(args, 1);
		final double z = getNumber(args, 2);

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
		module = PlethoraModules.KINETIC_S, target = EntitySkeleton.class,
		doc = "function(potency:number) -- Fire an arrow in the direction the skeleton is looking"
	)
	@Nonnull
	public static MethodResult shoot(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull final Object[] args) throws LuaException {
		final double potency = getNumber(args, 1);

		ArgumentHelper.assertBetween(potency, 0.1, 1.0, "Potency out of range (%s).");

		CostHelpers.checkCost(unbaked.getCostHandler(), Kinetic.shootCost * potency);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> context = unbaked.bake();
				EntitySkeleton skeleton = context.getContext(EntitySkeleton.class);

				ItemStack stack = skeleton.getHeldItem();
				if (stack == null || stack.getItem() != Items.bow) throw new LuaException("Not holding a bow");

				IWorldLocation location = context.getContext(IWorldLocation.class);

				EntityArrow arrow = new EntityArrow(location.getWorld(), skeleton, (float) (potency * 2));

				double damage = potency * 2;
				int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
				if (power > 0) damage += power * 0.5 + 0.5;
				arrow.setDamage(damage);

				int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
				if (punch > 0) arrow.setKnockbackStrength(punch);

				if (potency == 1.0) arrow.setIsCritical(true);

				if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0 || skeleton.getSkeletonType() == 1) {
					arrow.setFire(100);
				}

				skeleton.playSound("random.bow", 1.0F, 1.0F / (skeleton.getRNG().nextFloat() * 0.4F + 0.8F));

				location.getWorld().spawnEntityInWorld(arrow);
				return MethodResult.empty();
			}
		});
	}
}

