package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
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
					NetHandlerPlayServer handler = ((EntityPlayerMP) target).playerNetServerHandler;
					handler.setPlayerLocation(target.posX, target.posY, target.posZ, (float) yaw, (float) pitch);
				} else {
					target.rotationYawHead = target.rotationYaw = (float) (Math.toDegrees(yaw) % 360);
					target.rotationPitch = (float) (Math.toDegrees(pitch) % 360);
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
}
