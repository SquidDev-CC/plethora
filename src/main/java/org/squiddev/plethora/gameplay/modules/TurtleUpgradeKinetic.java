package org.squiddev.plethora.gameplay.modules;

import dan200.computercraft.api.turtle.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.core.TurtleUpgradeModule;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.integration.PlayerInteractionHelpers;
import org.squiddev.plethora.integration.computercraft.FakePlayerProviderTurtle;
import org.squiddev.plethora.utils.PlayerHelpers;

import javax.annotation.Nonnull;

public class TurtleUpgradeKinetic extends TurtleUpgradeModule {
	public TurtleUpgradeKinetic(@Nonnull ItemStack stack, @Nonnull IModuleHandler handler, @Nonnull String adjective) {
		super(stack, handler, adjective);
	}

	@Nonnull
	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Both;
	}

	@Nonnull
	@Override
	public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction) {
		if (isBlacklisted()) return TurtleCommandResult.failure();

		IPlayerOwnable ownable = new TurtlePlayerOwnable(turtle);
		PlethoraFakePlayer fakePlayer = FakePlayerProviderTurtle.getPlayer(turtle, ownable);

		FakePlayerProviderTurtle.load(fakePlayer, turtle, direction);
		try {
			RayTraceResult hit = PlayerHelpers.findHit(fakePlayer, 1.5);
			if (verb == TurtleVerb.Dig && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
				Pair<Boolean, String> previous = null;

				// We dig multiple times to make up for the delay that turtle.dig results
				// in
				for (int i = 0; i < 4; i++) {
					Pair<Boolean, String> result = fakePlayer.dig(hit.getBlockPos(), hit.sideHit);
					if (result.getLeft()) {
						previous = result;
					} else {
						return previous != null ? toResult(previous) : toResult(result);
					}
				}

				return toResult(previous);
			} else if (verb == TurtleVerb.Attack && hit.typeOfHit == RayTraceResult.Type.ENTITY) {
				return toResult(PlayerInteractionHelpers.attack(fakePlayer, hit.entityHit));
			} else {
				return TurtleCommandResult.failure("Nothing to do here");
			}
		} finally {
			fakePlayer.resetActiveHand();

			FakePlayerProviderTurtle.unload(fakePlayer, turtle);
			fakePlayer.updateCooldown();
		}
	}

	private static TurtleCommandResult toResult(Pair<Boolean, String> result) {
		return result.getLeft()
			? TurtleCommandResult.success(new Object[]{result.getRight()})
			: TurtleCommandResult.failure(result.getRight());
	}
}
