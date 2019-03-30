package org.squiddev.plethora.integration.vanilla.method;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.method.wrapper.FromContext;
import org.squiddev.plethora.api.method.wrapper.FromSubtarget;
import org.squiddev.plethora.api.method.wrapper.Optional;
import org.squiddev.plethora.api.method.wrapper.PlethoraMethod;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.PlayerInteractionHelpers;
import org.squiddev.plethora.integration.vanilla.FakePlayerProviderEntity;
import org.squiddev.plethora.utils.PlayerHelpers;

import java.util.Locale;

public final class MethodsKineticActions {
	private MethodsKineticActions() {
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "function([duration:integer], [hand:string]):boolean, string|nil -- Right click with this item using a " +
			"particular hand (\"left\" or \"right\"). The duration is in ticks, or 1/20th of a second."
	)
	public static MethodResult use(
		@FromSubtarget(ContextKeys.ORIGIN) EntityLivingBase entity,
		@Optional @FromContext(ContextKeys.ORIGIN) IPlayerOwnable ownable,
		@Optional(defInt = 0) int duration, @Optional String hand
	) throws LuaException {
		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");

		hand = hand == null ? "main" : hand.toLowerCase(Locale.ENGLISH);
		final EnumHand handE;
		switch (hand) {
			case "main":
			case "mainhand":
				handE = EnumHand.MAIN_HAND;
				break;
			case "off":
			case "offhand":
				handE = EnumHand.OFF_HAND;
				break;
			default:
				throw new LuaException("Unknown hand '" + hand + "', expected 'main' or 'off'");
		}

		EntityPlayerMP player;
		PlethoraFakePlayer fakePlayer;
		if (entity instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) entity;
			fakePlayer = null;
		} else if (entity instanceof EntityPlayer) {
			throw new LuaException("An unexpected player was used");
		} else {
			player = fakePlayer = FakePlayerProviderEntity.getPlayer(entity, ownable);
		}

		if (fakePlayer != null) FakePlayerProviderEntity.load(fakePlayer, entity);

		try {
			RayTraceResult hit = PlayerHelpers.findHit(player, entity);
			return PlayerInteractionHelpers.use(player, hit, handE, duration);
		} finally {
			if (fakePlayer != null) FakePlayerProviderEntity.unload(fakePlayer, entity);
		}
	}

	@PlethoraMethod(
		module = PlethoraModules.KINETIC_S,
		doc = "function():boolean, string|nil -- Left click with this item. Returns the action taken."
	)
	public static Object[] swing(
		@FromSubtarget(ContextKeys.ORIGIN) EntityLivingBase entity,
		@Optional @FromContext(ContextKeys.ORIGIN) IPlayerOwnable ownable
	) throws LuaException {
		EntityPlayerMP player;
		PlethoraFakePlayer fakePlayer;
		if (entity instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) entity;
			fakePlayer = null;
		} else if (entity instanceof EntityPlayer) {
			throw new LuaException("An unexpected player was used");
		} else {
			player = fakePlayer = FakePlayerProviderEntity.getPlayer(entity, ownable);
		}

		if (fakePlayer != null) FakePlayerProviderEntity.load(fakePlayer, entity);

		try {
			RayTraceResult hit = PlayerHelpers.findHit(player, entity);

			switch (hit.typeOfHit) {
				case ENTITY: {
					Pair<Boolean, String> result = PlayerInteractionHelpers.attack(player, hit.entityHit);
					return new Object[]{result.getLeft(), result.getRight()};
				}
				case BLOCK: {
					if (fakePlayer != null) {
						Pair<Boolean, String> result = fakePlayer.dig(hit.getBlockPos(), hit.sideHit);
						return new Object[]{result.getLeft(), result.getRight()};
					} else {
						return new Object[]{false, "Nothing to do here"};
					}
				}
				default:
					return new Object[]{false, "Nothing to do here"};
			}

		} finally {
			player.resetActiveHand();

			if (fakePlayer != null) {
				FakePlayerProviderEntity.unload(fakePlayer, entity);
				fakePlayer.updateCooldown();
			}
		}
	}
}
