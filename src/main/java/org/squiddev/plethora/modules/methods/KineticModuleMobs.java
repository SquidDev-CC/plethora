package org.squiddev.plethora.modules.methods;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.Method;
import org.squiddev.plethora.api.module.IModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.squiddev.plethora.ArgumentHelper.getNumber;

/**
 * Various methods for mobs
 */
public final class KineticModuleMobs {
	public static abstract class KineticMethodEntity<T extends EntityLivingBase> extends KineticModule.KineticMethod {
		private final Class<T> klass;

		public KineticMethodEntity(String name, Class<T> klass) {
			super(name);
			this.klass = klass;
		}

		@Override
		public boolean canApply(@Nonnull IContext<IModule> context) {
			return super.canApply(context) && context.hasContext(klass);
		}

		@Nullable
		@Override
		public final Object[] apply(@Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
			return apply(context.getContext(klass), context, args);
		}

		@Nullable
		public abstract Object[] apply(@Nonnull T target, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException;
	}

	@Method(IMethod.class)
	public static class KineticMethodEntityCreeperExplode extends KineticMethodEntity<EntityCreeper> {
		public KineticMethodEntityCreeperExplode() {
			super("explode", EntityCreeper.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityCreeper target, @Nonnull IContext<IModule> context, @Nonnull Object[] args) {
			target.explode();
			return null;
		}
	}

	@Method(IMethod.class)
	public static class KineticMethodEntityEndermanBlink extends KineticMethodEntity<EntityEnderman> {
		public KineticMethodEntityEndermanBlink() {
			super("teleport", EntityEnderman.class);
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull EntityEnderman target, @Nonnull IContext<IModule> context, @Nonnull Object[] args) throws LuaException {
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
