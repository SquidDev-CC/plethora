package org.squiddev.plethora.integration.vanilla.method;

import com.google.common.collect.Lists;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import org.squiddev.plethora.api.method.*;
import org.squiddev.plethora.api.module.IModule;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getNumber;
import static org.squiddev.plethora.api.method.ArgumentHelper.optNumber;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Modules.kineticLaunchMax;

public final class MethodsKinetic {
	@IMethod.Inject(IModule.class)
	public static final class MethodEntityLaunch extends TargetedModuleMethod<EntityLivingBase> {
		public MethodEntityLaunch() {
			super("launch", PlethoraModules.KINETIC, EntityLivingBase.class, "function(yaw:number, pitch:number, power:number) -- Launch the entity in a set direction");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final float yaw = (float) getNumber(args, 0);
			final float pitch = (float) getNumber(args, 1);
			final float power = (float) getNumber(args, 2);

			ArgumentHelper.assertBetween(power, 0, kineticLaunchMax, "Power out of range (%s).");

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					EntityLivingBase entity = context.bake().getContext(EntityLivingBase.class);
					ItemModule.launch(entity, yaw, pitch, power);
					return MethodResult.empty();
				}
			});
		}
	}

	@IMethod.Inject(IModule.class)
	public static final class MethodEntityLivingDisableAI extends TargetedModuleObjectMethod<EntityLiving> {
		public MethodEntityLivingDisableAI() {
			super("disableAI", PlethoraModules.KINETIC, EntityLiving.class, true, "function() -- Disable the AI of this entity");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving entity, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			// TODO: Call this every tick
			clearTasks(entity.tasks);
			clearTasks(entity.targetTasks);
			return null;
		}

		private static void clearTasks(EntityAITasks tasks) {
			List<EntityAITasks.EntityAITaskEntry> backup = Lists.newArrayList(tasks.taskEntries);
			for (EntityAITasks.EntityAITaskEntry entry : backup) {
				tasks.removeTask(entry.action);
			}
		}
	}

	@IMethod.Inject(IModule.class)
	public static final class MethodEntityWalk extends TargetedModuleMethod<EntityLiving> {
		public MethodEntityWalk() {
			super("walk", PlethoraModules.KINETIC, EntityLiving.class, "function(x:number, y:number, z:number]) -- Walk to a coordinate");
		}

		@Nonnull
		@Override
		public MethodResult apply(@Nonnull final IUnbakedContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			final double x = getNumber(args, 0);
			final double y = getNumber(args, 1);
			final double z = getNumber(args, 2);

			final double speed = optNumber(args, 3, 1);

			if (x < -32 || x > 32) throw new LuaException("X coordinate out of bounds (+-32");
			if (y < -32 || y > 32) throw new LuaException("Y coordinate out of bounds (+-32");
			if (z < -32 || z > 32) throw new LuaException("Z coordinate out of bounds (+-32");

			if (!Double.isNaN(speed) && speed < 1 || speed > 3) {
				throw new LuaException("Speed out of bounds (1 <= speed <= 3)");
			}

			return MethodResult.nextTick(new Callable<MethodResult>() {
				@Override
				public MethodResult call() throws Exception {
					EntityLiving living = context.bake().getContext(EntityLiving.class);
					PathNavigate navigator = living.getNavigator();

					PathEntity path = navigator.getPathToXYZ(
						x + living.posX,
						y + living.posY,
						z + living.posZ
					);

					if (path == null || path.getCurrentPathLength() == 0) {
						return MethodResult.failure("No path exists");
					}

					if (!context.getCostHandler().consume(path.getCurrentPathLength() * 5 * speed)) {
						return MethodResult.failure("Insufficient energy");
					}

					return MethodResult.result(living.getNavigator().setPath(path, speed));
				}
			});
		}
	}

	@IMethod.Inject(IModule.class)
	public static final class MethodEntityIsWalking extends TargetedModuleObjectMethod<EntityLiving> {
		public MethodEntityIsWalking() {
			super("isWalking", PlethoraModules.KINETIC, EntityLiving.class, true, "function():boolean -- Whether the entity is currently walking somewhere");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityLiving target, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			return new Object[]{!target.getNavigator().noPath()};
		}
	}
}
