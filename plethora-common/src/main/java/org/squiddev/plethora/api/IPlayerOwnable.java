package org.squiddev.plethora.api;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nullable;

/**
 * An object (such as a tile entity, entity, turtle, etc...) which can be
 * owned by a player. This is used for constructing fake players.
 */
public interface IPlayerOwnable {
	/**
	 * Get the profile for this player
	 *
	 * @return The profile for this player
	 */
	@Nullable
	GameProfile getOwningProfile();
}
