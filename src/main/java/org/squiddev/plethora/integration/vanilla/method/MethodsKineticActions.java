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
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.SubtargetedModuleMethod;
import org.squiddev.plethora.api.module.SubtargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.PlethoraFakePlayer;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.PlayerInteractionHelpers;
import org.squiddev.plethora.integration.vanilla.FakePlayerProviderEntity;
import org.squiddev.plethora.utils.PlayerHelpers;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.concurrent.Callable;

import static dan200.computercraft.core.apis.ArgumentHelper.optInt;
import static dan200.computercraft.core.apis.ArgumentHelper.optString;

public final class MethodsKineticActions {
	@SubtargetedModuleMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLivingBase.class,
		doc = "function([duration:integer], [hand:string]):boolean, string|nil -- Right click with this item using a " +
			"particular hand (\"left\" or \"right\"). The duration is in ticks, or 1/20th of a second."
	)
	public static MethodResult use(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final int duration = optInt(args, 0, 0);
		if (duration < 0) throw new LuaException("Duration out of range (must be >= 0)");

		String handStr = optString(args, 1, "main").toLowerCase(Locale.ENGLISH);
		final EnumHand hand;
		switch (handStr) {
			case "main":
			case "mainhand":
				hand = EnumHand.MAIN_HAND;
				break;
			case "off":
			case "offhand":
				hand = EnumHand.OFF_HAND;
				break;
			default:
				throw new LuaException("Unknown hand '" + handStr + "', expected 'main' or 'off'");
		}

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			@Nonnull
			public MethodResult call() throws Exception {
				IContext<?> baked = context.bake();
				EntityLivingBase entity = baked.getContext(ContextKeys.ORIGIN, EntityLivingBase.class);

				EntityPlayerMP player;
				PlethoraFakePlayer fakePlayer;
				if (entity instanceof EntityPlayerMP) {
					player = (EntityPlayerMP) entity;
					fakePlayer = null;
				} else if (entity instanceof EntityPlayer) {
					throw new LuaException("An unexpected player was used");
				} else {
					IPlayerOwnable ownable = baked.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
					player = fakePlayer = FakePlayerProviderEntity.getPlayer(entity, ownable);
				}

				if (fakePlayer != null) FakePlayerProviderEntity.load(fakePlayer, entity);

				try {
					RayTraceResult hit = PlayerHelpers.findHit(player, entity);
					return PlayerInteractionHelpers.use(player, hit, hand, duration);
				} finally {
					if (fakePlayer != null) FakePlayerProviderEntity.unload(fakePlayer, entity);
				}
			}
		});
	}

	@SubtargetedModuleObjectMethod.Inject(
		module = PlethoraModules.KINETIC_S, target = EntityLivingBase.class,
		doc = "function():boolean, string|nil -- Left click with this item. Returns the action taken."
	)
	public static Object[] swing(EntityLivingBase entity, IContext<IModuleContainer> context, Object[] args) throws LuaException {
		EntityPlayerMP player;
		PlethoraFakePlayer fakePlayer;
		if (entity instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) entity;
			fakePlayer = null;
		} else if (entity instanceof EntityPlayer) {
			throw new LuaException("An unexpected player was used");
		} else {
			IPlayerOwnable ownable = context.getContext(ContextKeys.ORIGIN, IPlayerOwnable.class);
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
