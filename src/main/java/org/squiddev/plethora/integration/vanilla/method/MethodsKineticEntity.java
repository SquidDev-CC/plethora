package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;

/**
 * Various methods for mobs
 */
public final class MethodsKineticEntity {
	@TargetedModuleMethod.Inject(
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

	@IMethod.Inject(IModuleContainer.class)
	public static final class MethodEntityCreeperExplode extends TargetedModuleObjectMethod<EntityCreeper> {
		public MethodEntityCreeperExplode() {
			super("explode", PlethoraModules.KINETIC, EntityCreeper.class, true, "function() -- Explode this creeper");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityCreeper target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) {
			target.explode();
			return null;
		}
	}

	@IMethod.Inject(IModuleContainer.class)
	public static final class MethodEntityEndermanTeleport extends TargetedModuleObjectMethod<EntityEnderman> {
		public MethodEntityEndermanTeleport() {
			super("teleport", PlethoraModules.KINETIC, EntityEnderman.class, true, "function(x:number, y:number, z:number) -- Teleport to a position relative to the current one");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityEnderman target, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
			double x = getNumber(args, 0);
			double y = getNumber(args, 1);
			double z = getNumber(args, 2);

			if (x < -32 || x > 32) throw new LuaException("X coordinate out of bounds (+-32");
			if (y < -32 || y > 32) throw new LuaException("Y coordinate out of bounds (+-32");
			if (z < -32 || z > 32) throw new LuaException("Z coordinate out of bounds (+-32");

			return new Object[]{target.teleportTo(target.posX + x, target.posY + y, target.posZ + z)};
		}
	}
}
