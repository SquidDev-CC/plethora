package org.squiddev.plethora.integration.ic2;

import dan200.computercraft.api.lua.LuaException;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.IC2;
import net.minecraft.tileentity.TileEntity;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.TypedLuaObject;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.reference.DynamicReference;
import org.squiddev.plethora.api.method.wrapper.FromTarget;

import javax.annotation.Nonnull;

public final class MethodsReactor {
	private MethodsReactor() {
	}

	private static class ReactorReference implements DynamicReference<IReactor> {
		private final IReactor core;
		private final TileEntity expected;

		ReactorReference(IReactor core) {
			this.core = core;
			expected = core.getCoreTe();
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
		public IReactor safeGet() {
			return core;
		}
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get a reference to the reactor's core")
	public static TypedLuaObject<IReactor> getReactorCore(IContext<IReactorChamber> context) {
		IReactor core = context.getTarget().getReactorInstance();
		return context.makeChild(core, new ReactorReference(core)).getObject();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get reactor heat")
	public static int getReactorHeat(@FromTarget IReactor reactor) {
		return reactor.getHeat();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get reactor max heat")
	public static int getReactorMaxHeat(@FromTarget IReactor reactor) {
		return reactor.getMaxHeat();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get reactor EU output")
	public static double getReactorEUOutput(@FromTarget IReactor reactor) {
		return reactor.getReactorEUEnergyOutput();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get if the reactor is active")
	public static boolean isReactorActive(@FromTarget IReactor reactor) {
		return reactor.produceEnergy();
	}

	@PlethoraMethod(modId = IC2.MODID, doc = "-- Get if the reactor is fluid cooled")
	public static boolean isFluidCooled(@FromTarget IReactor reactor) {
		return reactor.isFluidCooled();
	}
}
