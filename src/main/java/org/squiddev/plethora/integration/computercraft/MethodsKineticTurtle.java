package org.squiddev.plethora.integration.computercraft;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.PlayerInteractionHelpers;
import org.squiddev.plethora.utils.PlayerHelpers;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

import static dan200.computercraft.core.apis.ArgumentHelper.optInt;

public class MethodsKineticTurtle {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = ITurtleAccess.class,
		doc = "function([duration:integer]):boolean, string|nil -- Right click with this item."
	)
	public static MethodResult use(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final int duration = optInt(args, 0, 0);
		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			@Nonnull
			public MethodResult call() throws LuaException {
				IContext<IModuleContainer> baked = context.bake();
				ITurtleAccess turtle = baked.getContext(ContextKeys.ORIGIN, ITurtleAccess.class);

				IPlayerOwnable ownable = baked.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
				PlethoraFakePlayer fakePlayer = FakePlayerProviderTurtle.getPlayer(turtle, ownable);

				FakePlayerProviderTurtle.load(fakePlayer, turtle, turtle.getDirection());

				try {
					RayTraceResult hit = PlayerHelpers.findHit(fakePlayer, 1.5);
					return PlayerInteractionHelpers.use(fakePlayer, hit, EnumHand.MAIN_HAND, duration);
				} finally {
					FakePlayerProviderTurtle.unload(fakePlayer, turtle);
				}
			}
		});
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = ITurtleAccess.class, worldThread = true,
		doc = "function():boolean, string|nil -- Left click with this item. Returns the action taken."
	)
	public static Object[] swing(ITurtleAccess turtle, IContext<ITurtleAccess> context, Object[] args) {
		IPlayerOwnable ownable = context.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
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
