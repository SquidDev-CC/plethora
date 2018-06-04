package org.squiddev.plethora.integration.ic2;

import dan200.computercraft.api.lua.LuaException;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.IC2;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.reference.DynamicReference;

import javax.annotation.Nonnull;

public class MethodsReactor {
	private static class ReactorReference extends DynamicReference<IReactor> {
		private final IReactor core;
		private final TileEntity expected;

		ReactorReference(IReactor core) {
			this.core = core;
			this.expected = core.getCoreTe();
		}

		@Nonnull
		@Override
		public IReactor get() throws LuaException {
			if (core.getCoreTe() != expected) {
				throw new LuaException("The reactor has changed");
			}

			return core;
		}

		@Nonnull
		@Override
		public IReactor safeGet() throws LuaException {
			return core;
		}
	}

	@BasicObjectMethod.Inject(
			value = IReactorChamber.class, worldThread = true, modId = IC2.MODID,
			doc = "function():table -- Get a reference to the reactor's core"
	)
	public static Object[] getReactorCore(IContext<IReactorChamber> context, Object[] args) {
		IReactor core = context.getTarget().getReactorInstance();
		return new Object[] { context.makeChild(core, new ReactorReference(core)).getObject() };
	}

}
