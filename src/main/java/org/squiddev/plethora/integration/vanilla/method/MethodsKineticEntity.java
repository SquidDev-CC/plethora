package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.gen.FromSubtarget;
import org.squiddev.plethora.api.method.gen.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.getReal;
import static org.squiddev.plethora.api.method.ArgumentHelper.assertBetween;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Kinetic;

/**
 * Various methods for mobs
 */
public final class MethodsKineticEntity {
	@PlethoraMethod(module = PlethoraModules.KINETIC_S, doc = "-- Look in a set direction")
	@Nonnull
	public static void look(@FromSubtarget EntityLivingBase target, double pitch, double yaw) {
		yaw %= 360;
		pitch %= 360;

		if (target instanceof EntityPlayerMP) {
			NetHandlerPlayServer handler = ((EntityPlayerMP) target).connection;
			handler.setPlayerLocation(target.posX, target.posY, target.posZ, (float) yaw, (float) pitch);
		} else {
			target.rotationYawHead = target.rotationYaw = target.renderYawOffset = (float) (yaw % 360);
			target.rotationPitch = (float) (pitch % 360);
		}
	}

	@PlethoraMethod(module = PlethoraModules.KINETIC_S, doc = "-- Explode this creeper.")
	public static void explode(@FromSubtarget(ContextKeys.ORIGIN) EntityCreeper target) {
		target.explode();
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


		double cost = Math.sqrt(x * x + y * y + z * z) * Kinetic.teleportCost;
		return context.getCostHandler().await(cost, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> baked = context.bake();

			EntityEnderman target = baked.getContext(ContextKeys.ORIGIN, EntityEnderman.class);
			return MethodResult.result(target.teleportTo(target.posX + x, target.posY + y, target.posZ + z));
		}));
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = AbstractSkeleton.class, name = "shoot",
		doc = "function(potency:number) -- Fire an arrow in the direction the skeleton is looking"
	)
	@Nonnull
	public static MethodResult shootSkeleton(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull final Object[] args) throws LuaException {
		final double potency = getReal(args, 0);

		assertBetween(potency, 0.1, 1.0, "Potency out of range (%s).");

		return unbaked.getCostHandler().await(Kinetic.shootCost * potency, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> context = unbaked.bake();
			AbstractSkeleton skeleton = context.getContext(ContextKeys.ORIGIN, AbstractSkeleton.class);

			ItemStack stack = skeleton.getHeldItem(EnumHand.MAIN_HAND);
			if (stack.isEmpty() || stack.getItem() != Items.BOW) throw new LuaException("Not holding a bow");

			IWorldLocation location = context.getContext(IWorldLocation.class);

			EntityArrow arrow = (EntityArrow) ReflectionHelper
				.findMethod(AbstractSkeleton.class, "getArrow", "func_190726_a", float.class)
				.invoke(skeleton, (float) potency);

			float rotationYaw = skeleton.rotationYaw;
			float rotationPitch = skeleton.rotationPitch;
			float motionX = (-MathHelper.sin(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
			float motionZ = (MathHelper.cos(rotationYaw / 180.0f * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float) Math.PI));
			float motionY = (-MathHelper.sin(rotationPitch / 180.0f * (float) Math.PI));

			arrow.shoot(motionX, motionY, motionZ, 1.6f, (float) (potency * 2));

			double damage = potency * 2;
			int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
			if (power > 0) damage += power * 0.5 + 0.5;
			arrow.setDamage(damage);

			if (potency == 1.0) arrow.setIsCritical(true);

			skeleton.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (skeleton.getRNG().nextFloat() * 0.4F + 0.8F));

			location.getWorld().spawnEntity(arrow);
			return MethodResult.empty();
		}));
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityBlaze.class, name = "shoot",
		doc = "function(yaw:number, pitch:number) -- Fire a fireball in the specified direction."
	)
	@Nonnull
	public static MethodResult shootBlaze(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull final Object[] args) throws LuaException {
		final double yaw = getReal(args, 0) % 360;
		double pitch = getReal(args, 1) % 360;

		final double motionX = -Math.sin(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionZ = Math.cos(yaw / 180.0f * (float) Math.PI) * Math.cos(pitch / 180.0f * (float) Math.PI);
		final double motionY = -Math.sin(pitch / 180.0f * (float) Math.PI);

		return unbaked.getCostHandler().await(Kinetic.shootCost, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> context = unbaked.bake();
			EntityBlaze blaze = context.getContext(ContextKeys.ORIGIN, EntityBlaze.class);
			World world = blaze.getEntityWorld();

			world.playEvent(null, 1018, new BlockPos((int) blaze.posX, (int) blaze.posY, (int) blaze.posZ), 0); // ENTITY_BLAZE_SHOOT
			EntitySmallFireball fireball = new EntitySmallFireball(world, blaze, motionX, motionY, motionZ);
			fireball.posY = blaze.posY + (blaze.height / 2.0F) + 0.5D;
			world.spawnEntity(fireball);

			return MethodResult.empty();
		}));
	}

	private static final PotionType[] WITCH_POTIONS = {
		PotionTypes.HARMING,
		PotionTypes.SLOWNESS,
		PotionTypes.POISON,
		PotionTypes.WEAKNESS
	};

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityWitch.class, name = "shoot",
		doc = "function(potency:number) -- Throw a potion in the direction the witch is looking"
	)
	@Nonnull
	public static MethodResult shootWitch(@Nonnull final IUnbakedContext<IModuleContainer> unbaked, @Nonnull final Object[] args) throws LuaException {
		final double potency = getReal(args, 0);

		assertBetween(potency, 0.1, 1.0, "Potency out of range (%s).");

		return unbaked.getCostHandler().await(Kinetic.shootCost * potency, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> context = unbaked.bake();
			EntityWitch witch = context.getContext(ContextKeys.ORIGIN, EntityWitch.class);
			if (witch.isDrinkingPotion()) throw new LuaException("Currently drinking a potion");

			World world = witch.getEntityWorld();
			Vec3d motion = witch.getLookVec();

			PotionType potionType = WITCH_POTIONS[witch.getRNG().nextInt(WITCH_POTIONS.length)];
			EntityPotion potion = new EntityPotion(world, witch, PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potionType));
			potion.rotationPitch -= -20.0F;
			potion.shoot(motion.x, 0.2 + potency * 1.4, motion.y, 0.75F, 8.0F);
			world.playSound(null, witch.posX, witch.posY, witch.posZ, SoundEvents.ENTITY_WITCH_THROW, witch.getSoundCategory(), 1.0F, 0.8F + witch.getRNG().nextFloat() * 0.4F);
			world.spawnEntity(potion);

			return MethodResult.empty();
		}));
	}

	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityMinecart.class,
		doc = "function(velocity:number) -- Propel this minecart in along the track."
	)
	public static MethodResult propel(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		double given = getReal(args, 0);

		assertBetween(given, -Kinetic.propelMax, Kinetic.propelMax, "Velocity coordinate out of bounds (%s)");

		// We provide a number * 10 as it seems more "friendly".
		final double velocity = given * 0.1;

		return context.getCostHandler().await(Math.abs(given) * Kinetic.propelCost, MethodResult.nextTick(() -> {
			IContext<IModuleContainer> baked = context.bake();

			EntityMinecart target = baked.getContext(ContextKeys.ORIGIN, EntityMinecart.class);
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
		}));
	}
}

