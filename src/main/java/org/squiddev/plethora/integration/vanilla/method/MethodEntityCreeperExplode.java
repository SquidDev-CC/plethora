package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.monster.EntityCreeper;
import org.squiddev.plethora.api.method.BasicMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Method(EntityCreeper.class)
public class MethodEntityCreeperExplode extends BasicMethod<EntityCreeper> {
	public MethodEntityCreeperExplode() {
		super("explode", true);
	}

	@Nullable
	@Override
	public Object[] apply(@Nonnull IContext<EntityCreeper> context, @Nonnull Object[] args) throws LuaException {
		context.getTarget().explode();
		return null;
	}
}
