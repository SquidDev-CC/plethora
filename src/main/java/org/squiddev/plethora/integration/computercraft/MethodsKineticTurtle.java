package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.PlayerInteractionHelpers;
import org.squiddev.plethora.utils.PlayerHelpers;

public final class MethodsKineticTurtle {
	private MethodsKineticTurtle() {
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "function([duration:integer]):boolean, string|nil -- Right click with this item. The duration is in " +
			"ticks, or 1/20th of a second."
	)
	public static MethodResult use(
		IContext<IModuleContainer> context, @FromSubtarget(ContextKeys.ORIGIN) ITurtleAccess turtle,
		@Optional @FromContext(ContextKeys.ORIGIN) IPlayerOwnable ownable,
		@Optional(defInt = 0) int duration
	) throws LuaException {
		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");
		PlethoraFakePlayer fakePlayer = FakePlayerProviderTurtle.getPlayer(turtle, ownable);

		FakePlayerProviderTurtle.load(fakePlayer, turtle, turtle.getDirection());

		try {
			RayTraceResult hit = PlayerHelpers.findHit(fakePlayer, 1.5);
			return PlayerInteractionHelpers.use(fakePlayer, hit, EnumHand.MAIN_HAND, duration);
		} finally {
			FakePlayerProviderTurtle.unload(fakePlayer, turtle);
		}
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "function():boolean, string|nil -- Left click with this item. Returns the action taken."
	)
	public static Object[] swing(
		@FromSubtarget(ContextKeys.ORIGIN) ITurtleAccess turtle,
		@Optional @FromContext(ContextKeys.ORIGIN) IPlayerOwnable ownable
	) {
		PlethoraFakePlayer fakePlayer = FakePlayerProviderTurtle.getPlayer(turtle, ownable);

		FakePlayerProviderTurtle.load(fakePlayer, turtle, turtle.getDirection());
		try {
			RayTraceResult hit = PlayerHelpers.findHit(fakePlayer, 1.5);

			switch (hit.typeOfHit) {
				case ENTITY: {
					Pair<Boolean, String> result = PlayerInteractionHelpers.attack(fakePlayer, hit.entityHit);
					return new Object[]{result.getLeft(), result.getRight()};
				}
				case BLOCK: {
					Pair<Boolean, String> result = fakePlayer.dig(hit.getBlockPos(), hit.sideHit);
					return new Object[]{result.getLeft(), result.getRight()};
				}
			}

			return new Object[]{false, "Nothing to do here"};
		} finally {
			fakePlayer.resetActiveHand();

			FakePlayerProviderTurtle.unload(fakePlayer, turtle);
			fakePlayer.updateCooldown();
		}
	}
}
