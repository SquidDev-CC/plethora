package org.squiddev.plethora.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityPlayerDummy extends EntityPlayer {
	/**
	 * A UUID for demonstration purposes. It's a wee bit vain, but nice to have a "real" player here.
	 */
	private static final GameProfile PROFILE = new GameProfile(UUID.fromString("d3156e4b-c712-4fd3-87b0-b24b8ca94209"), "SquidDev");

	public EntityPlayerDummy(World worldIn) {
		super(worldIn, PROFILE);
	}

	@Override
	public boolean isSpectator() {
		return true;
	}

	@Override
	public boolean isCreative() {
		return false;
	}
}
